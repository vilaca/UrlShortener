/**
 * 
 */
package eu.vilaca.pagelets;

import com.sun.net.httpserver.HttpExchange;

import eu.vilaca.keystore.Database;
import eu.vilaca.keystore.HashKey;

/**
 * @author vilaca
 * 
 */
public class RedirectPageLet extends AbstractPageLet {

	private final byte[] buffer = new byte[0];

	@Override
	byte[] main(final HttpExchange exchange) {

		final String[] tokens = exchange.getRequestURI().getRawPath()
				.split("/");
		
		final HashKey hk = new HashKey(tokens[1].getBytes());

		final String url = Database.getDatabase().get(hk);

		if (url == null) {
			return null;
		}

		exchange.getResponseHeaders().set("Location", url);

		return buffer;
	}

	@Override
	public int getResponseCode() {
		return 302;
	}

	@Override
	public String getMimeType() {
		return "text/plain";
	}

}
