package pt.go2.application;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import pt.go2.keystore.KeyValueStore;
import pt.go2.pagelets.PageLet;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

class RequestHandler implements HttpHandler {

	private final KeyValueStore db;
	private final Map<String, PageLet> resources;
	private final String version;

	/**
	 * private c'tor to avoid external instantiation
	 * 
	 * @param properties
	 * 
	 * @param resources
	 *            mapping of URI to static content
	 */
	RequestHandler(KeyValueStore db, Map<String, PageLet> pages, String version) {

		// server will not at any case modify this structure
		this.resources = Collections.unmodifiableMap(pages);
		this.version = version;
		this.db = db;
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

		exchange.getResponseHeaders().set("Server",
				"Carapau de corrida " + version);

		final HttpResponse response = resource.getPageLet(exchange);

		final Headers headers = exchange.getResponseHeaders();

		if (response.isZipped()) {
			headers.set("Content-Encoding", "gzip");
		}

		headers.set("Content-Type", response.getMimeType());

		headers.set("Cache-Control", "max-age=" + 60 * 60 * 24); // cache for a whole day
		
		exchange.sendResponseHeaders(response.getHttpErrorCode(),
				response.getSize());

		final OutputStream os = exchange.getResponseBody();

		os.write(response.getBody());

		os.flush();
		os.close();

		Server.printLogMessage(exchange, response);
	}

	/**
	 * Resolve URI to correct page/resource or use 404
	 * 
	 * @param exchange
	 *            .getRequestURI()
	 * @return
	 */
	private PageLet getPageContents(final HttpExchange exchange) {

		final String filename = getRequestedFilename(exchange.getRequestURI());

		final PageLet page;
		
		if (filename.length() == 6) {
			page = db.get(filename);
		}
		else
		{
			page = resources.get(filename);
		}

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
