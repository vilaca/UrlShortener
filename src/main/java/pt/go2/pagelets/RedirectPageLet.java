package pt.go2.pagelets;

import java.io.IOException;

import pt.go2.application.HttpResponse;

import com.sun.net.httpserver.HttpExchange;

/**
 * 301 status Redirection. Not used to redirec hashes
 */
public class RedirectPageLet implements PageLet {

	private final HttpResponse response;
	
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
