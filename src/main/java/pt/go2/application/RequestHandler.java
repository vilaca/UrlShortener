package pt.go2.application;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.handler.AbstractHandler;

import pt.go2.fileio.Configuration;
import pt.go2.response.AbstractResponse;

public abstract class RequestHandler extends AbstractHandler {

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
	public RequestHandler(final Configuration config,
			final Resources vfs, final BufferedWriter accessLog) {

		this.accessLog = accessLog;
		this.config = config;
		this.vfs = vfs;
	}

	/**
	 * Stream Http Response
	 * @param request 
	 * 
	 * @param exchange
	 * @param response
	 * @throws IOException
	 */

	protected void reply(HttpServletRequest request, final HttpServletResponse exchange,
			final AbstractResponse response, final boolean cache)
			throws IOException {

		final byte[] body = response.run(exchange);

		final int status = response.getHttpStatus();

		setHeaders(exchange, response, cache);

		exchange.setStatus(status);

		printLogMessage(request, exchange, response, body.length);
	}

	/**
	 * Set response headers
	 * @param exchange 
	 * 
	 * @param response
	 * @param headers
	 */
	private void setHeaders(HttpServletResponse exchange,
			final AbstractResponse response, final boolean cache) {

		exchange.setHeader(AbstractResponse.RESPONSE_HEADER_SERVER,
				"Carapau de corrida " + config.VERSION);

		exchange.setHeader(AbstractResponse.RESPONSE_HEADER_CONTENT_TYPE,
				response.getMimeType());

		// TODO only static files should be cached
		if (cache) {

			exchange.setHeader(AbstractResponse.RESPONSE_HEADER_CACHE_CONTROL,
					"max-age=" + TimeUnit.HOURS.toSeconds(config.CACHE_HINT));

		} else {
			exchange.setHeader(AbstractResponse.RESPONSE_HEADER_CACHE_CONTROL,
					"no-cache, no-store, must-revalidate");

			exchange.setHeader(AbstractResponse.RESPONSE_HEADER_EXPIRES, "0");
		}

		if (response.isZipped()) {
			exchange.setHeader(
					AbstractResponse.RESPONSE_HEADER_CONTENT_ENCODING, "gzip");
		}
	}
		
	/**
	 * Access log output
	 * @param request 
	 * @param exchange 
	 * @param exchange 
	 * 
	 * @param params
	 * @param response
	 */
	protected void printLogMessage(
			HttpServletRequest request, HttpServletResponse exchange, final AbstractResponse response, final int size) {

		final StringBuilder sb = new StringBuilder();

		sb.append(request.getRemoteAddr());
		sb.append(" - - [");
		sb.append(new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z")
				.format(new Date()));
		sb.append("] \"");
		sb.append(request.getMethod());
		sb.append(" ");
		sb.append(request.getRequestURI());
		sb.append(" ");
		sb.append(request.getProtocol());
		sb.append("\" ");
		sb.append(response.getHttpStatus());
		sb.append(" ");
		sb.append(size);
		sb.append(" \"");

		final String referer = request.getHeader(AbstractResponse.REQUEST_HEADER_REFERER);

		final String agent = request.getHeader(AbstractResponse.REQUEST_HEADER_USER_AGENT);

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
