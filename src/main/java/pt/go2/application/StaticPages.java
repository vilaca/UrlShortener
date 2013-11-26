package pt.go2.application;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.fileio.Configuration;
import pt.go2.fileio.Statistics;
import pt.go2.keystore.HashKey;
import pt.go2.keystore.Uri;
import pt.go2.response.AbstractResponse;
import pt.go2.response.RedirectResponse;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

/**
 * Handles server requests
 */
class StaticPages extends AbstractHandler {

	static final Logger logger = LogManager.getLogger(StaticPages.class);

	final Calendar calendar = Calendar.getInstance();

	private final Statistics statistics;

	/**
	 * C'tor
	 * 
	 * @param config
	 * @param vfs
	 * @param statistics
	 * @throws IOException
	 */
	public StaticPages(final Configuration config, final Resources vfs,
			Statistics statistics, final BufferedWriter accessLog) {
		super(config, vfs, accessLog);

		this.statistics = statistics;
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

			reply(exchange, vfs.get(Resources.Error.BAD_REQUEST), false);
			return;
		}

		// redirect to out domain if host header is not correct

		if (!correctHost(request)) {

			reply(exchange, vfs.get(Resources.Error.REJECT_SUBDOMAIN), false);
			return;
		}

		String requested = exchange.getRequestURI().getRawPath();

		if (requested.length() == 7) {

			requested = requested.substring(1);

			final String referer = exchange.getRequestHeaders().getFirst(
					AbstractResponse.REQUEST_HEADER_REFERER);

			final String ip = exchange.getRemoteAddress().getAddress()
					.getHostAddress();

			statistics.add(ip, requested, referer, calendar.getTime());

			final Uri uri = vfs.get(new HashKey(requested.substring(1)));

			if (uri == null) {
				reply(exchange, vfs.get(Resources.Error.PAGE_NOT_FOUND), true);
				return;
			}

			if (vfs.isBanned(uri)) {
				logger.warn("banned: " + uri);
				reply(exchange, vfs.get(Resources.Error.FORBIDDEN_PHISHING),
						true);
				return;
			}

			reply(exchange, new RedirectResponse(uri.toString(), 301), true);
			return;
		}

		AbstractResponse response = vfs.get(requested);

		if (response == null)
			response = vfs.get(Resources.Error.PAGE_NOT_FOUND);

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
}
