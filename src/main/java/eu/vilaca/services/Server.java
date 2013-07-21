/**
 * 
 */
package eu.vilaca.services;

import java.io.FileNotFoundException;
import java.io.IOException;
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

import eu.vilaca.pagelets.PageLet;
import eu.vilaca.pagelets.ShortenerPageLet;
import eu.vilaca.pagelets.StaticPageLet;

/**
 * @author vilaca
 * 
 */
class Server {

	static final Logger logger = LogManager.getLogger(Server.class.getName());
	
	// resource file locations on JAR
	
	private static final String base = "/";
	
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

		InetSocketAddress sockAddress;

		Properties prop;
		try {
			
			prop = loadPropertiesFile(base +"application.properties");
		
		} catch (IOException e1) {
			
			logger.fatal("Can't read properties.");
			return;
		}

		// database must reload from files
		
		try {
			Database.start(prop.getProperty("database.folder"));
		} catch (IOException e1) {
			System.out.println("Database unstable.");
			return;			
		}
		
		// get bind setting from propertie files		
		
		try {

			sockAddress = createInetSocketAddress(prop);

		} catch (NumberFormatException ex) {

			return;
		}

		// create listener
		
		final HttpServer listener;
		try {

			listener = HttpServer.create(sockAddress, 0);

		} catch (IOException e) {

			e.printStackTrace();
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
		
		listener.stop(1);

		try {
			Database.stop();
		} catch (IOException e) {
		}
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
			final Properties prop) throws IllegalArgumentException {

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
	 * @param string
	 * @param prop
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private static Properties loadPropertiesFile(String filename)
			throws IOException {
		final Properties prop = new Properties();
		prop.load(Server.class.getResourceAsStream(filename));
		return prop;
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

		sb.append(params.getRemoteAddress());
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

		// TODO don't stream to console

		System.out.println(sb.toString());
	}
}
