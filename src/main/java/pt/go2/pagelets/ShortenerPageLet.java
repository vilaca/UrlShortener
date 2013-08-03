/**
 * 
 */
package pt.go2.pagelets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import pt.go2.keystore.Database;

import com.sun.net.httpserver.HttpExchange;


/**
 * @author vilaca
 * 
 */
public class ShortenerPageLet extends AbstractPageLet {

	@Override
	public byte[] main(final HttpExchange params) throws IOException {

		try (final InputStream is = params.getRequestBody();
				final InputStreamReader sr = new InputStreamReader(is);
				final BufferedReader br = new BufferedReader(sr);) {

			final String postBody = br.readLine();

			if ( postBody == null ) throw new IOException("Badly formed Request Body.");
			
			// format for form content is 'fieldname=value'
			final String[] formContents = postBody.split("=");

			byte[] response = Database.getDatabase().add(formContents[1]);
			
			return response == null ? "BAD-URI".getBytes() : response;
		}

	}

	@Override
	public int getResponseCode() {
		return 200;
	}

	@Override
	public String getMimeType() {
		return "text/plain";
	}
}
