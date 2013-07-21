package eu.vilaca.services;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import eu.vilaca.pagelets.PageLet;
import eu.vilaca.pagelets.RedirectPageLet;

class RequestHandler implements HttpHandler {

	private final Map<String, PageLet> resources;

	/**
	 * private c'tor to avoid external instantiation
	 * 
	 * @param resources
	 *            mapping of URI to static content
	 */
	RequestHandler(final Map<String, PageLet> pages) {

		// server will not at any case modify this structure
		this.resources = Collections.unmodifiableMap(pages);
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

		final PageLet resource = getPageContents(exchange);

		if (resource == null || !resource.execute(exchange)) {

			// if no page was found use page 404

			resources.get("404").execute(exchange);

			Server.printLogMessage(exchange, 404);

			return;
		}

		Server.printLogMessage(exchange, resource.getResponseCode());
	}

	/**
	 * Resolve URI to correct page/resource
	 * 
	 * @param exchange
	 *            .getRequestURI()
	 * @return
	 */
	private PageLet getPageContents(final HttpExchange exchange) {

		final String filename = getRequestedFilename(exchange.getRequestURI());

		if (filename.length() == 6) {
			return new RedirectPageLet();
		}

		return resources.get(filename);
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
