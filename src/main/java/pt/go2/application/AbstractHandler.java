package pt.go2.application;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import pt.go2.fileio.Configuration;
import pt.go2.response.AbstractResponse;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public abstract class AbstractHandler implements HttpHandler {

	protected final Resources vfs;
	protected final Configuration config;
	private final BufferedWriter accessLog;

	/**
	 * C'tor
	 * 
	 * @param accessLog
	 * @param config
	 * @param accessLog
	 * @throws IOException
	 */
	public AbstractHandler(final Configuration config,
			final Resources vfs, final BufferedWriter accessLog) {

		this.accessLog = accessLog;
		this.config = config;
		this.vfs = vfs;
	}

	/**
	 * Stream Http Response
	 * 
	 * @param exchange
	 * @param response
	 * @throws IOException
	 */
	protected void reply(final HttpExchange exchange,
			final AbstractResponse response, final boolean cache)
			throws IOException {

		final Headers headers = exchange.getResponseHeaders();

		final byte[] body = response.run(exchange);

		final int size = body.length;
		final int status = response.getHttpStatus();

		setHeaders(response, headers, cache);

		exchange.sendResponseHeaders(status, size);

		final OutputStream os = exchange.getResponseBody();

		os.write(body);

		os.flush();
		os.close();

		printLogMessage(exchange, response, size);
	}

	/**
	 * Set response headers
	 * 
	 * @param response
	 * @param headers
	 */
	private void setHeaders(final AbstractResponse response,
			final Headers headers, final boolean cache) {

		headers.set(AbstractResponse.RESPONSE_HEADER_SERVER,
				"Carapau de corrida " + config.VERSION);

		headers.set(AbstractResponse.RESPONSE_HEADER_CONTENT_TYPE,
				response.getMimeType());

		// TODO only static files should be cached
		if (cache) {

			headers.set(AbstractResponse.RESPONSE_HEADER_CACHE_CONTROL,
					"max-age=" + TimeUnit.HOURS.toSeconds(config.CACHE_HINT));

		} else {
			headers.set(AbstractResponse.RESPONSE_HEADER_CACHE_CONTROL,
					"no-cache, no-store, must-revalidate");

			headers.set(AbstractResponse.RESPONSE_HEADER_EXPIRES, "0");
		}

		if (response.isZipped()) {
			headers.set(AbstractResponse.RESPONSE_HEADER_CONTENT_ENCODING,
					"gzip");
		}
	}

	/**
	 * Is the request for this domain?
	 * 
	 * (Used to redirect from www.go2.pt to go2.pt)
	 * 
	 * @param headers
	 * @return
	 */
	protected boolean correctHost(final Headers headers) {
		return headers.getFirst(AbstractResponse.REQUEST_HEADER_HOST)
				.startsWith(config.ENFORCE_DOMAIN);
	}

	/**
	 * Access log output
	 * 
	 * @param params
	 * @param response
	 */
	protected void printLogMessage(final HttpExchange params,
			final AbstractResponse response, final int size) {

		final StringBuilder sb = new StringBuilder();

		sb.append(params.getRemoteAddress().getAddress().getHostAddress());
		sb.append(" - - [");
		sb.append(new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z")
				.format(new Date()));
		sb.append("] \"");
		sb.append(params.getRequestMethod());
		sb.append(" ");
		sb.append(params.getRequestURI().toString());
		sb.append(" ");
		sb.append(params.getProtocol());
		sb.append("\" ");
		sb.append(response.getHttpStatus());
		sb.append(" ");
		sb.append(size);
		sb.append(" \"");

		final Headers headers = params.getRequestHeaders();
		
		final String referer = headers
				.getFirst(AbstractResponse.REQUEST_HEADER_REFERER);

		final String agent = headers
				.getFirst(AbstractResponse.REQUEST_HEADER_USER_AGENT);

		sb.append(referer == null ? "-" : referer);

		sb.append("\" \"" + agent + "\"");
		sb.append(System.getProperty("line.separator"));

		final String output = sb.toString();

		if (accessLog == null) {
			System.out.print(output);
			return;
		}

		try {
			accessLog.write(output);
			accessLog.flush();
		} catch (IOException e) {
		}
	}

}
