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
import pt.go2.keystore.KeyValueStore;
import pt.go2.keystore.Uri;
import pt.go2.response.HtmlResponse;

@Page(requireLogin = false, path = "api/link/shorten/")
public class Shorten extends AbstractHandler {

	static private final Logger LOG = LogManager.getLogger(Shorten.class);

	@Injected
	private Resources vfs;

	@Injected
	private KeyValueStore ks;

	@Override
	public void handle() throws IOException {

		final String field = "v";
		final List<String> fields = Arrays.asList(new String[] { field });
		final Map<String, String> values = new HashMap<>(fields.size());

		try {
			if (!parseForm(values, fields, null)) {
				reply(ErrorMessages.Error.BAD_REQUEST);
				return;
			}

			final String v = values.get(field);

			if (v == null) {
				reply(ErrorMessages.Error.BAD_REQUEST);
				return;
			}

			// Parse string into Uri

			final Uri uri = Uri.create(v, true);

			if (uri == null) {
				reply(ErrorMessages.Error.BAD_REQUEST);
				return;
			}

			// Refuse banned

			if (vfs.isBanned(uri)) {
				LOG.warn("banned: " + uri + " - " + getHostName());
				reply(ErrorMessages.Error.FORBIDDEN_PHISHING_AJAX);
				return;
			}

			// hash Uri

			final byte[] hashedUri = ks.add(uri);

			if (hashedUri.length == 0) {
				reply(ErrorMessages.Error.BAD_REQUEST);
				return;
			}

			reply(new HtmlResponse(hashedUri));

		} catch (IOException e) {
			reply(ErrorMessages.Error.BAD_REQUEST);
			return;
		}
	}
}
