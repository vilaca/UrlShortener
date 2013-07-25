package eu.vilaca.services;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import eu.vilaca.keystore.Database;
import eu.vilaca.pagelets.AbstractPageLet;

class RequestHandler implements HttpHandler {

	private final Map<String, AbstractPageLet> resources;
	private final String version;

	/**
	 * private c'tor to avoid external instantiation
	 * 
	 * @param properties
	 * 
	 * @param resources
	 *            mapping of URI to static content
	 */
	RequestHandler(final Map<String, AbstractPageLet> pages, Properties properties) {

		// server will not at any case modify this structure
		this.resources = Collections.unmodifiableMap(pages);
		this.version = properties.getProperty("server.version", "unversioned");
	}
	

	/**
	 * Handle request, parse URI filename from request into page resource
	 * 
	 * @param
	 * 
	 * @exception IOException
	 */
	@Override
	public void handle(final HttpExchange exchange) throws IOException {

		final AbstractPageLet resource = getPageContents(exchange);

		resource.execute(exchange);

		exchange.getResponseHeaders().set("Server", version);
		
		Server.printLogMessage(exchange, resource);
	}

	/**
	 * Resolve URI to correct page/resource or use 404
	 * 
	 * @param exchange
	 *            .getRequestURI()
	 * @return
	 */
	private AbstractPageLet getPageContents(final HttpExchange exchange) {

		final String filename = getRequestedFilename(exchange.getRequestURI());

		if (filename.length() == 6) {
			return Database.getDatabase().get(filename);
		}

		final AbstractPageLet page = resources.get(filename);
		
		return page != null ? page : resources.get("404");
	}

	/**
	 * Parse requested filename from URI
	 * 
	 * @param request
	 * 
	 * @return Requested filename
	 */
	private String getRequestedFilename(final URI request) {

		// split into tokens

		final String[] tokens = request.getRawPath().split("/");

		if (tokens.length > 0) {
			return tokens[1];
		}

		// empty URI (no tokens) means front page

		return "/";
	}

}
