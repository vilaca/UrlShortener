package pt.go2.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.annotations.Injected;
import pt.go2.annotations.Page;
import pt.go2.application.Resources;
import pt.go2.keystore.Uri;
import pt.go2.response.HtmlResponse;

import com.sun.net.httpserver.HttpExchange;

@Page(requireLogin = false, path = "api/link/shorten/")
public class Shorten extends AbstractFormHandler {

	static private final Logger LOG = LogManager.getLogger(Shorten.class);

	@Injected
	private Resources vfs;
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {

		final String field = "v";
		final List<String> fields = Arrays.asList(new String[] { field });
		final Map<String, String> values = new HashMap<>(fields.size());

		try {
			if (!parseForm(exchange, values, fields, null)) {
				reply(exchange, vfs.get(Resources.Error.BAD_REQUEST), false);
				return;
			}

			final String v = values.get(field);

			if (v == null) {
				reply(exchange, vfs.get(Resources.Error.BAD_REQUEST), false);
				return;
			}

			// Parse string into Uri

			final Uri uri = Uri.create(v, true);

			if (uri == null) {
				reply(exchange, vfs.get(Resources.Error.BAD_REQUEST), false);
				return;
			}

			// Refuse banned

			if (vfs.isBanned(uri)) {
				LOG.warn("banned: " + uri + " - "
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
