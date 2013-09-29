/**
 * 
 */
package pt.go2.services;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.fileio.PageLetFileReader;
import pt.go2.keystore.Database;
import pt.go2.pagelets.PageLet;
import pt.go2.pagelets.ShortenerPageLet;
import pt.go2.pagelets.StaticPageLet;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * @author vilaca
 * 
 */
class Server {

	static private final Logger logger = LogManager.getLogger(Server.class
			.getName());

	static private final Properties properties = PropertiesManager
			.getProperties();

	static private final Database db = Database.getDatabase();

	static private BufferedWriter accessLog = null;

	/**
	 * Process initial method
	 */
	public static void main(final String[] args) {

		logger.trace("Starting server...");

		// log server version

		logger.trace("Preparing to run "
				+ properties.getProperty("server.version", "undefined"));

		try {
			final HttpServer listener = prepareServer();

			if (listener == null) {
				return;
			}

			listener.setExecutor(null); // creates a default executor
			listener.start();

			System.out.println("Server Running. Press [k] to kill listener.");
			boolean running = true;
			do {

				try {
					running = System.in.read() == 'k';
				} catch (IOException e) {
				}

			} while (running);

			logger.trace("Server stopping.");

			listener.stop(1);
			
		} finally {
			
			try {
				db.stop();
			} catch (IOException e) {
			}

			try {
				if (accessLog != null)
				{
					accessLog.close();
				}
			} catch (IOException e) {
			}
			logger.trace("Server stopped.");
		}

	}

	private static HttpServer prepareServer() {
		// map static pages to URI part

		final Map<String, PageLet> pages;
		try {
			pages = generateResourceDecoder();
		} catch (IOException ex) {
			logger.fatal(ex.getMessage());
			return null;
		}

		// database must reload hashes

		try {

			final String resumeFolder = properties
					.getProperty("database.folder");

			if (resumeFolder == null) {
				logger.error("database.folder not found on .properties");
			}

			db.start(resumeFolder);

		} catch (IOException e1) {
			logger.fatal("Database unstable.");
			return null;
		}

		// start access_log

		try {
			final String filename = properties.getProperty("server.accessLog");
			if (filename != null) {
				accessLog = new BufferedWriter(new FileWriter(filename, true));
			}
		} catch (IOException e1) {
			System.out.println("Access log redirected to console.");
		}

		// get bind setting from properties files
		InetSocketAddress sockAddress;
		try {
			sockAddress = createInetSocketAddress(properties);
		} catch (Exception ex) {
			logger.fatal("Bad parameters for server address/port.");
			return null;
		}

		// create listener

		logger.trace("Server now starting....");

		final HttpServer listener;
		try {

			// get backlog from properties too
			final int backlog = getBacklog(properties);
			listener = HttpServer.create(sockAddress, backlog);

		} catch (IOException e) {
			logger.fatal("Could not create listener.");
			return null;
		}

		// configure server context to /
		// and use default executor (single-threaded)

		listener.createContext("/", new RequestHandler(pages, properties));
		return listener;
	}

	private static int getBacklog(Properties prop) {

		final String _backlog = prop.getProperty("server.backlog");

		// default to 0

		if (_backlog != null) {
			try {
				return Integer.parseInt(_backlog);
			} catch (NumberFormatException ex) {
			}
		}

		return 0;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private static Map<String, PageLet> generateResourceDecoder()
			throws IOException {

		final PageLetFileReader fr = new PageLetFileReader("/");
		final Map<String, PageLet> pages = new HashMap<String, PageLet>();

		pages.put("/", 
				new StaticPageLet.Builder()
						.setContent(fr.read("index.html")).zip().build());

		pages.put("ajax.js",
				new StaticPageLet.Builder()
						.setContent(fr.read("ajax.js"))
						.setMimeType("application/javascript").zip().build());

		pages.put("robots.txt",
				new StaticPageLet.Builder()
						.setContent(fr.read("robots.txt"))
						.setMimeType("text/plain").zip().build());

		pages.put("sitemap.xml",
				new StaticPageLet.Builder()
						.setContent(fr.read("map.txt"))
						.setMimeType("text/xml").zip().build());

		pages.put("screen.css",
				new StaticPageLet.Builder()
						.setContent(fr.read("screen.css"))
						.setMimeType("text/css").zip().build());

		// dynamic pages
		pages.put("new", new ShortenerPageLet());

		// error pages
		pages.put("404",
				new StaticPageLet.Builder()
						.setContent(fr.read("404.html"))
						.setResponseCode(404).zip().build());
		
		return pages;
	}

	/**
	 * @param prop
	 * @return
	 * @throws NumberFormatException
	 */
	private static InetSocketAddress createInetSocketAddress(
			final Properties prop) {

		// both parameters are optional

		final String host = prop.getProperty("server.host");
		final String port = prop.getProperty("server.port");

		// default port is 80

		final int _port;
		if (port == null) {
			_port = 80;
		} else {
			_port = Integer.parseInt(port);
		}

		if (host == null) {
			return new InetSocketAddress(_port);
		} else {
			return new InetSocketAddress(host, _port);
		}
	}

	/**
	 * Access log output
	 * 
	 * @param params
	 * @param response
	 */
	static void printLogMessage(final HttpExchange params,
			final HttpResponse response) {

		final StringBuilder sb = new StringBuilder();

		sb.append(params.getRemoteAddress().getAddress().getHostAddress());
		sb.append(" - - [");
		sb.append(new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss Z")
				.format(new Date()));
		sb.append("] \"");
		sb.append(params.getRequestMethod());
		sb.append(" ");
		sb.append(params.getRequestURI().toString());
		sb.append(" HTTP/1.0\" ");
		sb.append(response.getHttpErrorCode());
		sb.append(" ");
		sb.append(response.getSize());
		sb.append(" \"");

		final String referer = params.getRequestHeaders().getFirst("Referer");
		sb.append(referer == null ? "-" : referer);

		sb.append("\" \"browser info discarded\"");
		sb.append(System.getProperty("line.separator"));

		final String output = sb.toString();

		if (accessLog != null) {
			try {
				accessLog.write(output);
				accessLog.flush();
				return;
			} catch (IOException e) {
			}
		}

		System.out.print(output);

	}
}
