package pt.go2.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.application.Resources;
import pt.go2.keystore.Uri;
import pt.go2.response.HtmlResponse;

import com.sun.net.httpserver.HttpExchange;

// replaced by Shorten 
@Deprecated
class UrlHashing extends AbstractHandler {

	static private final Logger logger = LogManager.getLogger(UrlHashing.class);

	private Resources vfs;

	/**
	 * Handle shortening of Urls.
	 * 
	 * If Url already exists return hash. If Url wasn't hashed before generate
	 * hash and add it to value store
	 */
	@Deprecated
	@Override
	public void handle(HttpExchange exchange) throws IOException {

		try (final InputStream is = exchange.getRequestBody();
				final InputStreamReader sr = new InputStreamReader(is);
				final BufferedReader br = new BufferedReader(sr);) {

			// read body content

			final String postBody = br.readLine();

			if (postBody == null) {
				reply(exchange, vfs.get(Resources.Error.BAD_REQUEST), false);
				return;
			}

			// format for form content is 'fieldname=value'

			final int idx = postBody.indexOf('=') + 1;

			if (idx == -1 || postBody.length() - idx < 3) {
				reply(exchange, vfs.get(Resources.Error.BAD_REQUEST), false);
				return;
			}

			// Parse string into Uri

			final Uri uri = Uri.create(postBody.substring(idx), true);

			if (uri == null) {
				reply(exchange, vfs.get(Resources.Error.BAD_REQUEST), false);
				return;
			}

			// Refuse banned

			if (vfs.isBanned(uri)) {
				logger.warn("banned: " + uri + " - "
						+ exchange.getRemoteAddress().getHostName());
				reply(exchange,
						vfs.get(Resources.Error.FORBIDDEN_PHISHING_AJAX), false);
				return;
			}

			// hash Uri

			final byte[] hashedUri = vfs.add(uri);

			if (hashedUri.length == 0) {
				reply(exchange, vfs.get(Resources.Error.BAD_REQUEST), false);
				return;
			}

			reply(exchange, new HtmlResponse(hashedUri), false);

		} catch (IOException e) {
			reply(exchange, vfs.get(Resources.Error.BAD_REQUEST), false);
			return;
		}
	}
}
