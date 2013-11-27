package pt.go2.response;

import com.sun.net.httpserver.HttpExchange;

/**
 * Http Redirect
 */
public class RedirectResponse extends AbstractResponse {

	private final String redirect;
	private final int status;

	public RedirectResponse(final String redirect, final int status) {

		this.redirect = redirect;
		this.status = status;
	}

	@Override
	public int getHttpStatus() {
		return status;
	}

	@Override
	public byte[] run(HttpExchange exchange) {
		exchange.getResponseHeaders().set(RESPONSE_HEADER_LOCATION, redirect);
		return new byte[] {};
	}
}
