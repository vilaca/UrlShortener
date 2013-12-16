package pt.go2.application;

import java.io.IOException;

import pt.go2.api.AbstractHandler;
import pt.go2.fileio.Configuration;
import pt.go2.response.RedirectResponse;

/**
 * The role of this handler is to redirect HTTP requests to HTTPS
 * 
 */
public class HttpsEnforcer extends AbstractHandler {

	private Configuration config;

	HttpsEnforcer(final Configuration config) {
		this.config = config;
	}

	public void handle() throws IOException {

		final String redirect = "https://" + config.ENFORCE_DOMAIN + getRawPath();
		reply(new RedirectResponse(redirect, 301));
	}
}
