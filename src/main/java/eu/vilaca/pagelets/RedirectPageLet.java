/**
 * 
 */
package eu.vilaca.pagelets;

import com.sun.net.httpserver.HttpExchange;

import eu.vilaca.services.Database;
import eu.vilaca.services.HashKey;

/**
 * @author vilaca
 * 
 */
public class RedirectPageLet extends PageLet {

	private final byte[] buffer = new byte[0];

	@Override
	byte[] main(final HttpExchange exchange) {

		final String[] tokens = exchange.getRequestURI().getRawPath().split("/");
		final HashKey hk = new HashKey(tokens[1].getBytes());
		
		final String url = Database.get(hk);
		
		if ( url == null)
		{
			return null;
		}
		
		exchange.getResponseHeaders().set("Location", url);
		
		return buffer;
	}

	@Override
	public int getResponseCode() {
		return 302;
	}

}
