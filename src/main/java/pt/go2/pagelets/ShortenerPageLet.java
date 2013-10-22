package pt.go2.pagelets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import pt.go2.application.HttpResponse;
import pt.go2.keystore.KeyValueStore;
import pt.go2.keystore.Uri;

import com.sun.net.httpserver.HttpExchange;

/**
 * This pagelet handles a POST request, gets the URL in its body and returns an
 * Hashed representation of the URL
 */
public class ShortenerPageLet implements PageLet {

	// hash/url keystore reference
	
	private final KeyValueStore db;

	/**
	 * C'tor
	 * 
	 * @param db
	 */
	public ShortenerPageLet(final KeyValueStore db) {
		this.db = db;
	}

	@Override
	public HttpResponse getPageLet(final HttpExchange params)
			throws IOException {

		try (final InputStream is = params.getRequestBody();
				final InputStreamReader sr = new InputStreamReader(is);
				final BufferedReader br = new BufferedReader(sr);) {

			final String postBody = br.readLine();

			if (postBody == null) {
				return HttpResponse.createBadRequest();
			}

			// format for form content is 'fieldname=value'

			final int idx = postBody.indexOf('=') + 1;

			if (idx == -1 || postBody.length() - idx < 3) {
				return HttpResponse.createBadRequest();
			}

			final Uri uri = Uri.create(postBody.substring(idx));

			if (uri == null) {
				return HttpResponse.createBadRequest();
			}

			final byte[] hashedUri = db.add(uri);

			if (hashedUri == null) {
				return HttpResponse.createBadRequest();
			}

			HttpResponse response = HttpResponse.create("text/plain",
					hashedUri, 200);

			return response;
		}
	}
}
