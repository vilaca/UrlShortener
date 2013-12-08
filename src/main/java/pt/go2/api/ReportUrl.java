package pt.go2.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.annotations.Injected;
import pt.go2.annotations.Page;
import pt.go2.application.AbstractHandler;
import pt.go2.application.Resources;
import pt.go2.keystore.HashKey;
import pt.go2.response.AbstractResponse;
import pt.go2.response.SimpleResponse;

import com.sun.net.httpserver.HttpExchange;

/**
 * Reported Url handler
 */
@Page(requireLogin = false, path = "api/report/")
public class ReportUrl extends AbstractHandler {

	private static final Logger LOG = LogManager.getLogger(ReportUrl.class);

	private static final String VALID_CHARS = "^[a-zA-Z0-9]*$";

	private static final String URL = "url=";
	private static final String REASON = "reason=";
	
	@Injected
	private Resources resources;

	/**
	 * Get Url and Reason from POST request
	 * 
	 * Fields: [url] & [reason]
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {

		String url = null;
		String reason = null;

		try (final InputStream is = exchange.getRequestBody();
				final InputStreamReader sr = new InputStreamReader(is);
				final BufferedReader br = new BufferedReader(sr);) {

			// read body content

			do {

				final String postBody = br.readLine();

				if (postBody == null) {
					reply(exchange, resources.get(Resources.Error.BAD_REQUEST), false);
					return;
				}

				if (url == null && postBody.startsWith(URL)) {
					url = postBody.substring(URL.length());
					continue;
				}

				if (reason == null && postBody.startsWith(REASON)) {
					reason = postBody.substring(REASON.length());
					continue;
				}

				reply(exchange, resources.get(Resources.Error.BAD_REQUEST), false);
				return;

			} while (url == null && reason == null);
		}

		if (url.length() != 6 || reason.length() > 10
				|| !url.matches(VALID_CHARS) || !reason.matches(VALID_CHARS)) {

			LOG.info("Failed to Report Url: " + url + ", " + reason);

			reply(exchange, resources.get(Resources.Error.BAD_REQUEST), false);
		}

		if (resources.get(new HashKey(url)) == null) {

			LOG.info("Reported Url: " + url + " does not exist.");

			reply(exchange, resources.get(Resources.Error.BAD_REQUEST), false);
		}

		LOG.warn("Reported Url: " + url + ", " + reason);

		reply(exchange, new SimpleResponse(200,
				AbstractResponse.MIME_TEXT_PLAIN), false);
	}
}
