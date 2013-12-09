package pt.go2.application;

import java.io.IOException;

import pt.go2.annotations.Injected;
import pt.go2.api.AbstractHandler;
import pt.go2.fileio.Configuration;
import pt.go2.response.RedirectResponse;

import com.sun.net.httpserver.HttpExchange;

/**
 * The role of this handler is to redirect HTTP requests to HTTPS
 * 
 */
public class HttpsEnforcer extends AbstractHandler {

	@Injected
	private Configuration config;

	public void handle(HttpExchange exchange) throws IOException {

		final String requested = exchange.getRequestURI().getRawPath();
		final String redirect = "https://" + config.ENFORCE_DOMAIN + requested;

		reply(exchange, new RedirectResponse(redirect, 301), true);
	}
}
