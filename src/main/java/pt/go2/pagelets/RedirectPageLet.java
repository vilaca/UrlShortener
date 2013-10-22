package pt.go2.pagelets;

import java.io.IOException;

import pt.go2.application.HttpResponse;

import com.sun.net.httpserver.HttpExchange;

/**
 * @author vilaca
 * 
 */
public class RedirectPageLet implements PageLet {

	private final HttpResponse response;
	private final String redirect;

	public RedirectPageLet(int responseCode, String redirect) {
		this.response = HttpResponse.create("text/html", "".getBytes(),
				responseCode);
		this.redirect = redirect;
	}

	@Override
	public HttpResponse getPageLet(HttpExchange exchange) throws IOException {
		exchange.getResponseHeaders().set("Location", redirect);
		return response;
	}
}
