package pt.go2.application;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import pt.go2.fileio.Configuration;
import pt.go2.response.AbstractResponse;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

/**
 * Handles server requests
 */
class StaticPages extends AbstractHandler {

	/**
	 * C'tor
	 * 
	 * @param config
	 * @param vfs
	 * @throws IOException
	 */
	public StaticPages(final Configuration config,
			final VirtualFileSystem vfs, final BufferedWriter accessLog) {
		super(config, vfs, accessLog);
	}

	/**
	 * Handle request, parse URI filename from request into page resource
	 * 
	 * @param
	 * 
	 * @exception IOException
	 */
	@Override
	public void handle(final HttpExchange exchange) throws IOException {

		final Headers request = exchange.getRequestHeaders();

		// we need a host header to continue

		if (!validRequest(request)) {

			reply(exchange, vfs.get(VirtualFileSystem.Error.BAD_REQUEST), false);
			return;
		}

		// redirect to out domain if host header is not correct

		if (!correctHost(request)) {

			reply(exchange, vfs.get(VirtualFileSystem.Error.REJECT_SUBDOMAIN), false);
			return;
		}

		final String requested = getRequestedFilename(exchange.getRequestURI());

		final AbstractResponse response = vfs.getPage(requested);

		reply(exchange, response, true);
	}

	/**
	 * Server needs a Host header
	 * 
	 * @param headers
	 * @return
	 */
	private boolean validRequest(final Headers headers) {
		return headers.get(AbstractResponse.REQUEST_HEADER_HOST).size() > 0;
	}

	/**
	 * Parse requested filename from URI
	 * 
	 * @param request
	 * 
	 * @return Requested filename
	 */
	private String getRequestedFilename(final URI request) {

		// split into tokens

		final String path = request.getRawPath();

		if (path.equals("/")) {
			return path;
		}

		final int idx = path.indexOf('/', 1);

		return idx == -1 ? path.substring(1) : path.substring(1, idx);
	}
}
