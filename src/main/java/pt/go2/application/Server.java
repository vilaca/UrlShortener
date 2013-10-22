package pt.go2.application;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.fileio.Configuration;
import pt.go2.fileio.SmartTagParser;
import pt.go2.keystore.KeyValueStore;
import pt.go2.pagelets.PageLet;
import pt.go2.pagelets.ShortenerPageLet;
import pt.go2.pagelets.StaticPageLetBuilder;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class Server {

	static private final Configuration config = new Configuration();
	
	static private final Logger logger = LogManager.getLogger(Server.class
			.getName());

	static private BufferedWriter accessLog = null;

	static private KeyValueStore ks;

	
	/**
	 * Process initial method
	 */
	public static void main(final String[] args) {

		logger.trace("Starting server...");

		// log server version

		logger.trace("Preparing to run " + config.VERSION + ".");

		logger.trace("Current relative path is:" + config.RELATIVE_PATH);
		logger.trace("Resuming DB from folder: " + config.DATABASE_FOLDER);

		// restore URI/hash mappings data

		ks = KeyValueStore.create(config.DATABASE_FOLDER, config.REDIRECT);
			
		// map static pages to URI part

		final Map<String, PageLet> pages;
		try {
			pages = generateResourceDecoder();
		} catch (IOException ex) {
			logger.fatal("Error reading resources from file." + ex.getMessage());
			return;
		}
			
		// create listener

		logger.trace("Server now starting....");

		final HttpServer listener;
		try {

			listener = HttpServer.create(config.HOST, config.BACKLOG);

		} catch (IOException e) {
			logger.fatal("Could not create listener.");
			return;
		}

		listener.createContext("/", new RequestHandler(ks, pages, config.VERSION));
		listener.setExecutor(null);

		try {
			
			// start access log

			try {
				final FileWriter file = new FileWriter(config.ACCESS_LOG, true);
				accessLog = new BufferedWriter(file);
			} catch (IOException e1) {
				System.out.println("Access log redirected to console.");
				return;
			}

			// start server

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
				if (accessLog != null) {
					accessLog.close();
				}
			} catch (IOException e) {
			}
			logger.trace("Server stopped.");
		}
	}

	/**
	 * URL to Pagelet Multiplexer
	 * 
	 * @return
	 * @throws IOException
	 */
	private static Map<String, PageLet> generateResourceDecoder()
			throws IOException {

		final SmartTagParser fr = new SmartTagParser("/");
		final Map<String, PageLet> pages = new HashMap<String, PageLet>();

		pages.put("/", 
				new StaticPageLetBuilder()
						.setContent(fr.read("index.html")).zip().build());

		pages.put("ajax.js",
				new StaticPageLetBuilder()
						.setContent(fr.read("ajax.js"))
						.setMimeType("application/javascript").zip().build());

		pages.put("robots.txt",
				new StaticPageLetBuilder()
						.setContent(fr.read("robots.txt"))
						.setMimeType("text/plain").zip().build());

		pages.put("sitemap.xml",
				new StaticPageLetBuilder()
						.setContent(fr.read("map.txt"))
						.setMimeType("text/xml").zip().build());

		pages.put("screen.css",
				new StaticPageLetBuilder()
						.setContent(fr.read("screen.css"))
						.setMimeType("text/css").zip().build());

		// dynamic pages
		pages.put("new", new ShortenerPageLet(ks));

		// error pages
		pages.put("404",
				new StaticPageLetBuilder()
						.setContent(fr.read("404.html"))
						.setResponseCode(404).zip().build());
		
		return pages;
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
