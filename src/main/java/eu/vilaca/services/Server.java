/**
 * 
 */
package eu.vilaca.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import eu.vilaca.keystore.Database;
import eu.vilaca.pagelets.PageLet;
import eu.vilaca.pagelets.ShortenerPageLet;
import eu.vilaca.pagelets.StaticPageLet;

/**
 * @author vilaca
 * 
 */
class Server {

	static final Logger logger = LogManager.getLogger(Server.class.getName());
	static BufferedWriter accessLog;

	// resource file locations on JAR

	private static final String base = "/";
	private static final String PROPERTIES = "application.properties";

	/**
	 * Process initial method
	 */
	public static void main(final String[] args) {

		logger.trace("Entering application.");

		// map static pages to URI part

		final Map<String, PageLet> pages;
		try {
			pages = generateResourceDecoder();
		} catch (IOException ex) {
			logger.fatal(ex.getMessage());
			return;
		}

		final Properties properties = loadPropertiesFile();
		if (properties == null) {
			logger.fatal("Can't read properties.");
			return;
		}

		// database must reload hashes

		try {
			Database.start(properties.getProperty("database.folder"));
		} catch (IOException e1) {
			logger.fatal("Database unstable.");
			return;
		}

		// start access_log

		try {
			final String filename = properties.getProperty("access_log");
			if (filename != null) {
				accessLog = new BufferedWriter(new FileWriter(filename));
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
			return;
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
			return;
		}

		// configure server context to /
		// and use default executor (single-threaded)

		listener.createContext("/", new RequestHandler(pages));
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

		try {
			Database.stop();
		} catch (IOException e) {
		}

		try {
			accessLog.close();
		} catch (IOException e) {
		}

		logger.trace("Server stopped.");
	}

	private static Properties loadPropertiesFile() {
		InputStream is = null;
		final Properties prop = new Properties();
		try {

			is = new File(PROPERTIES).exists() ? new FileInputStream(PROPERTIES)
					: Server.class.getResourceAsStream(base + PROPERTIES);

			prop.load(is);

		} catch (IOException e1) {
			return null;
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
			}
		}
		return prop;
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
		final Map<String, PageLet> pages;
		// static pages

		pages = new HashMap<String, PageLet>();

		pages.put("/", StaticPageLet.fromFile(base + "index.html"));

		pages.put("ajax.js", StaticPageLet.fromFile(base + "ajax.js"));

		pages.put("robots.txt", StaticPageLet.fromFile(base + "robots.txt"));

		pages.put("sitemap.xml", StaticPageLet.fromFile(base + "map.txt"));

		pages.put("style.css", StaticPageLet.fromFile(base + "style.css"));

		// dynamic pages
		pages.put("new", new ShortenerPageLet());

		// error pages
		pages.put("404", StaticPageLet.fromFile(base + "404.html", 404));
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
	 * @param responseCode
	 */
	static void printLogMessage(final HttpExchange params,
			final int responseCode) {

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
		sb.append(responseCode);
		sb.append(" 435 \"-\" \"browser info ignored\"");

		final String output = sb.toString();

		if (accessLog != null) {
			try {
				accessLog.write(output);
				accessLog.flush();
				return;
			} catch (IOException e) {
			}
		}

		System.out.println(output);

	}
}
