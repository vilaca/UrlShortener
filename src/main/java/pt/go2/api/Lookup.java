package pt.go2.api;

import java.io.IOException;

import pt.go2.annotations.Injected;
import pt.go2.annotations.Page;
import pt.go2.application.Resources;
import pt.go2.keystore.HashKey;
import pt.go2.keystore.KeyValueStore;
import pt.go2.keystore.Uri;
import pt.go2.response.AbstractResponse;
import pt.go2.response.SimpleResponse;

@Page(requireLogin = false, path = "api/url/lookup/")
public class Lookup extends AbstractHandler {

	@Injected
	private Resources vfs;

	@Injected
	private KeyValueStore ks;

	@Override
	public void handle() throws IOException {

		final String request = getHttpExchange().getRequestURI().getPath();

		final String fields[] = request.split("/");

		if (fields.length < 3) {
			reply(ErrorMessages.Error.BAD_REQUEST);
			return;
		}

		final String hash = fields[2];

		if (hash.isEmpty()) {
			reply(ErrorMessages.Error.BAD_REQUEST);
			return;
		}

		final HashKey hk = new HashKey(hash);

		final Uri uri = ks.get(hk);

		if (uri == null) {
			reply(ErrorMessages.Error.HASH_NOT_FOUND);
		} else {
			reply(new SimpleResponse(uri.toString().getBytes(), 200,
					AbstractResponse.MIME_TEXT_PLAIN));
		}
	}
}
