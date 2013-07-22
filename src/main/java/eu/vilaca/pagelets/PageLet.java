/**
 * 
 */
package eu.vilaca.pagelets;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;

/**
 * @author vilaca
 * 
 */
public abstract class PageLet {

	abstract byte[] main(final HttpExchange exchange) throws IOException;

	abstract public int getResponseCode();

	final public boolean execute(final HttpExchange exchange)
			throws IOException {

		final byte[] buffer = main(exchange);

		if (buffer == null)
			return false;

		final OutputStream os = exchange.getResponseBody();

		exchange.sendResponseHeaders(getResponseCode(), buffer.length);

		os.write(buffer);
		os.flush();
		os.close();

		return true;
	}

}
