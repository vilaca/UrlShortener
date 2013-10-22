package pt.go2.pagelets;

import java.io.IOException;

import pt.go2.application.HttpResponse;

import com.sun.net.httpserver.HttpExchange;

/**
 * Redirection response (Http code 301/2). Points to an Hashed URL
 */
public class RedirectPageLet implements PageLet {

	private final HttpResponse response;
	
	// Redirect code. Either 301 or 302.
	
	private final String redirect;
	
	// Empty response body
	
	private final byte[] emptyBody = "".getBytes();

	public RedirectPageLet(int responseCode, String redirect) {
		this.response = HttpResponse.create("text/html", emptyBody,
				responseCode);
		this.redirect = redirect;
	}

	@Override
	public HttpResponse getPageLet(HttpExchange exchange) throws IOException {
		exchange.getResponseHeaders().set("Location", redirect);
		return response;
	}
}
