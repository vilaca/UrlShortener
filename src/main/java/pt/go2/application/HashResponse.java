package pt.go2.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import pt.go2.keystore.KeyValueStore;
import pt.go2.keystore.Uri;

import com.sun.net.httpserver.HttpExchange;

/**
 * Used when the client asks to hash an Url 
 */
class HashResponse extends AbstractResponse {

	final KeyValueStore ks;
	byte[] body;
	int status;

	HashResponse(final KeyValueStore ks) {
		this.ks = ks;
	}

	@Override
	public int getHttpStatus() {
		return status;
	}

	@Override
	byte[] run(HttpExchange exchange) {

		try (final InputStream is = exchange.getRequestBody();
				final InputStreamReader sr = new InputStreamReader(is);
				final BufferedReader br = new BufferedReader(sr);) {

			// read body content
			
			final String postBody = br.readLine();

			if (postBody == null) {
				return badRequest();
			}

			// format for form content is 'fieldname=value'

			final int idx = postBody.indexOf('=') + 1;

			if (idx == -1 || postBody.length() - idx < 3) {
				return badRequest();
			}

			// Parse string into Uri
			
			final Uri uri = Uri.create(postBody.substring(idx), true);

			if (uri == null) {
				return badRequest();
			}

			// hash Uri
			
			final byte[] hashedUri = ks.add(uri);

			if (hashedUri == null) {
				return badRequest();
			}

			status = 200;
			return hashedUri;

		} catch (IOException e) {
			return badRequest();
		}
	}

	private byte[] badRequest() {
		status = 400;
		return "".getBytes();
	}
}
