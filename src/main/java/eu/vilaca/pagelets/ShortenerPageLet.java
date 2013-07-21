/**
 * 
 */
package eu.vilaca.pagelets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import com.sun.net.httpserver.HttpExchange;

import eu.vilaca.services.Database;

/**
 * @author vilaca
 * 
 */
public class ShortenerPageLet extends PageLet {

	@Override
	public byte[] main (final HttpExchange params) throws IOException {

		try (final InputStream is = params.getRequestBody();
				final InputStreamReader sr = new InputStreamReader(is);
				final BufferedReader br = new BufferedReader(sr);) {

			final String postBody = br.readLine();

			// format for form content is 'fieldname=value'
			final String[] formContents = postBody.split("=");

			return Database.add(formContents[1]);
		}

	}

	@Override
	public int getResponseCode() {
		return 200;
	}
}
