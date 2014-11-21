package pt.go2.application;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import pt.go2.application.ErrorPages.Error;
import pt.go2.fileio.Configuration;
import pt.go2.response.AbstractResponse;
import pt.go2.response.RedirectResponse;

public abstract class RequestHandler extends AbstractHandler {

	static final Logger logger = LogManager.getLogger();

	protected final Configuration config;
	private final BufferedWriter accessLog;
	private final ErrorPages errors;

	public RequestHandler(Configuration config, BufferedWriter accessLog,
			ErrorPages errors) {

		this.accessLog = accessLog;
		this.config = config;
		this.errors = errors;
	}

	/**
	 * Stream Http Response
	 * 
	 * @param request
	 * 
	 * @param exchange
	 * @param response
	 * @throws IOException
	 */
	protected void reply(HttpServletRequest request,
			final HttpServletResponse exchange,
			final AbstractResponse response, final boolean cache) {

		final byte[] body = response.run(exchange);

		int status = response.getHttpStatus();

		setHeaders(exchange, response, cache);

		exchange.setStatus(status);

		try (ServletOutputStream stream = exchange.getOutputStream()) {

			stream.write(body);
			stream.flush();

		} catch (IOException e) {

			// TODO ???
			status = 500;
		}

		printLogMessage(status, request, exchange, response, body.length);
	}

	protected void reply(HttpServletRequest request,
			HttpServletResponse exchange, Error badRequest, boolean cache) {
		reply(request, exchange, errors.get(badRequest), cache);
	}

	/**
	 * Set response headers
	 * 
	 * @param exchange
	 * 
	 * @param response
	 * @param headers
	 */
	private void setHeaders(HttpServletResponse exchange,
			final AbstractResponse response, final boolean cache) {

		// TODO ? does this still work ?
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
	 * 
	 * @param status
	 * 
	 * @param request
	 * @param exchange
	 * @param exchange
	 * 
	 * @param params
	 * @param response
	 */
	protected void printLogMessage(int status, HttpServletRequest request,
			HttpServletResponse exchange, final AbstractResponse response,
			final int size) {

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
		sb.append(status);
		sb.append(" ");
		sb.append(size);
		sb.append(" \"");

		final String referer = request
				.getHeader(AbstractResponse.REQUEST_HEADER_REFERER);

		final String agent = request
				.getHeader(AbstractResponse.REQUEST_HEADER_USER_AGENT);

		sb.append(referer == null ? "-" : referer);

		sb.append("\" \"" + agent + "\"");
		sb.append(System.getProperty("line.separator"));

		final String output = sb.toString();

		if (accessLog == null) {
			System.out.print(output);
			return;
		}

		try {
			synchronized (this) {
				accessLog.write(output);
				accessLog.flush();
			}
		} catch (IOException e) {
		}
	}

	@Override
	public void handle(String dontcare, Request base,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		base.setHandled(true);

		// we need a host header to continue

		final String host = request
				.getHeader(AbstractResponse.REQUEST_HEADER_HOST);

		if (host.isEmpty()) {
			reply(request, response, Error.BAD_REQUEST, false);
			return;
		}

		// redirect to out domain if host header is not correct

		final String enforce = config.ENFORCE_DOMAIN;

		if (enforce != null && !enforce.isEmpty() && !host.startsWith(enforce)) {

			reply(request, response, new RedirectResponse("//" + enforce, 301),
					false);

			logger.error("Wrong host: " + host);
			return;
		}

		handle(request, response);
	}

	abstract public void handle(HttpServletRequest req, HttpServletResponse res);
}
