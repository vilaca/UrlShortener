package pt.go2.api;

import java.io.IOException;

import pt.go2.annotations.Injected;
import pt.go2.annotations.Page;
import pt.go2.application.AbstractHandler;
import pt.go2.application.Resources;
import pt.go2.keystore.HashKey;
import pt.go2.keystore.Uri;
import pt.go2.response.AbstractResponse;
import pt.go2.response.SimpleResponse;

import com.sun.net.httpserver.HttpExchange;

@Page(requireLogin = false, path = "api/url/lookup/")
public class Lookup extends AbstractHandler {

	@Injected
	protected Resources vfs;
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {

		final String request = exchange.getRequestURI().getPath();

		final String fields[] = request.split("/");

		if (fields.length < 3) {
			reply(exchange, vfs.get(Resources.Error.BAD_REQUEST), false);
			return;
		}

		final String hash = fields[2];

		if (hash.isEmpty()) {
			reply(exchange, vfs.get(Resources.Error.BAD_REQUEST), false);
			return;
		}

		final HashKey hk = new HashKey(hash);

		final Uri uri = vfs.get(hk);

		if (uri == null) {
			reply(exchange, vfs.get(Resources.Error.HASH_NOT_FOUND), false);
		} else {
			reply(exchange, new SimpleResponse(uri.toString().getBytes(), 200,
					AbstractResponse.MIME_TEXT_PLAIN), false);
		}
	}
}
