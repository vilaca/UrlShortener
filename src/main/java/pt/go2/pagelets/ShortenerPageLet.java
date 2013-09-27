/**
 * 
 */
package pt.go2.pagelets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import pt.go2.keystore.Database;
import pt.go2.services.HttpResponse;

import com.sun.net.httpserver.HttpExchange;

/**
 * @author vilaca
 * 
 */
public class ShortenerPageLet implements PageLet {

	private static final Database db = Database.getDatabase();

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

			final String[] formContents = postBody.split("=");

			if (formContents.length < 2) {
				return HttpResponse.createBadRequest();
			}

			final byte[] hashedUri = db.add(formContents[1]);

			if (hashedUri == null) {
				return HttpResponse.createBadRequest();
			}

			HttpResponse response = HttpResponse.create("text/plain",
					hashedUri, 200);

			return response;
		}
	}
}
