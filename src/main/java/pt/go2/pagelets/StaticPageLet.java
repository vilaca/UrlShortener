package pt.go2.pagelets;

import java.io.IOException;
import java.util.List;
import pt.go2.application.HttpResponse;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

/**
 * Immutable class for static pagelets like index.html, css, etc  
 */
public class StaticPageLet implements PageLet {

	final private HttpResponse response;
	final private HttpResponse compressedResponse;

	/**
	 * C'tor. Use StaticPageLetBuilder instead.
	 * 
	 * @param content
	 * 
	 * @param mimeType
	 * 
	 * @param responseCode
	 * 
	 * @param gzipped
	 */
	StaticPageLet(final byte[] content, final String mimeType,
			final int responseCode, final byte[] gzipped) {
		this.response = HttpResponse.create(mimeType, content, responseCode);
		this.compressedResponse = HttpResponse.createZipped(mimeType, gzipped,
				responseCode);
	}

	@Override
	public HttpResponse getPageLet(final HttpExchange exchange)
			throws IOException {

		if (this.compressedResponse != null) {

			if (clientAcceptsZip(exchange)) {
				return this.compressedResponse;
			}
		}

		return this.response;
	}

	/**
	 * Does the client accept a ziped response?
	 * 
	 * @param exchange
	 * 
	 * @return
	 */
	private boolean clientAcceptsZip(final HttpExchange exchange) {
		final Headers headers = exchange.getRequestHeaders();
		final List<String> values = headers.get("Accept-encoding");
		final boolean sendZipped = values.size() > 0
				&& values.get(0).indexOf("gzip") != -1;
		return sendZipped;
	}
}
