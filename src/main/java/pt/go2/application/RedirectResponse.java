package pt.go2.application;

import com.sun.net.httpserver.HttpExchange;

/**
 * Http Redirect
 */
class RedirectResponse extends AbstractResponse {

	final String redirect;
	final int status;

	RedirectResponse(final String redirect, final int status) {

		this.redirect = redirect;
		this.status = status;
	}

	@Override
	public int getHttpStatus() {
		return 301;
	}

	@Override
	byte[] run(HttpExchange exchange) {
		exchange.getResponseHeaders().set(RESPONSE_HEADER_LOCATION, redirect);
		return "".getBytes();
	}
}
