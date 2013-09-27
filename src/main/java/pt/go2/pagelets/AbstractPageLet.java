/**
 * 
 */
package pt.go2.pagelets;

import java.io.IOException;
import java.io.OutputStream;

import pt.go2.services.HttpResponse;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

/**
 * @author vilaca
 * 
 */
public abstract class AbstractPageLet {

	abstract HttpResponse getPageLet(final HttpExchange exchange)
			throws IOException;

	final public HttpResponse execute(final HttpExchange exchange)
			throws IOException {

		// execute abstract method of PageLet

		final HttpResponse response = getPageLet(exchange);

		if (!response.success()) {
			final Headers headers = exchange.getResponseHeaders();

			headers.set("Content-Type", response.getMimeType());

			exchange.sendResponseHeaders(response.getHttpErrorCode(),
					response.getSize());
		} else {
			final OutputStream os = exchange.getResponseBody();
			final Headers headers = exchange.getResponseHeaders();

			headers.set("Content-Type", response.getMimeType());

			exchange.sendResponseHeaders(response.getHttpErrorCode(),
					response.getSize());

			os.write(response.getBody());

			os.flush();
			os.close();
		}
		return response;

	}
}
