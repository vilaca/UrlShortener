package pt.go2.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import pt.go2.annotations.Injected;
import pt.go2.api.ErrorMessages.Error;
import pt.go2.fileio.Configuration;
import pt.go2.response.AbstractResponse;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public abstract class AbstractHandler implements HttpHandler {

	@Injected
	private BufferedWriter accessLog;

	@Injected
	ErrorMessages errors;

	@Injected
	Configuration config;

	@Injected
	Statistics statistics;

	private HttpExchange exchange;

	abstract public void handle() throws IOException;

	@Override
	public final void handle(HttpExchange exchange) throws IOException {

		final Headers request = exchange.getRequestHeaders();
		this.exchange = exchange;

		// we need a host header to continue

		if (!validRequest(request)) {

			reply(errors.get(Error.BAD_REQUEST));
			return;
		}

		// redirect to out domain if host header is not correct

		if (!correctHost(request)) {

			reply(errors.get(Error.REJECT_SUBDOMAIN));
			return;
		}

		handle();
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
	 * Stream Http Response
	 * 
	 * @param exchange
	 * @param response
	 * @throws IOException
	 */
	protected void reply(final ErrorMessages.Error e) throws IOException {
		reply(errors.get(e));
	}

	/**
	 * Stream Http Response
	 * 
	 * @param exchange
	 * @param response
	 * @throws IOException
	 */
	protected void reply(final AbstractResponse response) throws IOException {

		final Headers headers = exchange.getResponseHeaders();

		final byte[] body = response.run(exchange);

		final int size = body.length;
		final int status = response.getHttpStatus();

		setHeaders(response, headers);

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
			final Headers headers) {

		headers.set(AbstractResponse.RESPONSE_HEADER_SERVER,
				"Carapau de corrida " + config.VERSION);

		headers.set(AbstractResponse.RESPONSE_HEADER_CONTENT_TYPE,
				response.getMimeType());

		if (getCache()) {

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

	protected boolean parseForm(final Map<String, String> values,
			List<String> fields, final UserMan users) throws IOException {

		int remaining = fields.size();

		try (final InputStream is = exchange.getRequestBody();
				final InputStreamReader sr = new InputStreamReader(is);
				final BufferedReader br = new BufferedReader(sr);) {

			// read body content

			do {

				final String line = br.readLine();

				if (line == null) {
					reply(ErrorMessages.Error.BAD_REQUEST);
					return false;
				}

				int idx = line.indexOf('=');

				if (idx == -1) {
					reply(ErrorMessages.Error.BAD_REQUEST);
					return false;
				}

				final String field = line.substring(0, idx);

				idx = fields.indexOf(field);

				if (idx == -1) {
					reply(ErrorMessages.Error.BAD_REQUEST);
					return false;
				}

				final String value = line.substring(idx + 1);

				if (users != null && !users.validateUserProperty(field, value)) {
					reply(ErrorMessages.Error.BAD_REQUEST);
					return false;
				}

				final String prev = values.put(field, value);

				if (prev != null) {
					reply(ErrorMessages.Error.BAD_REQUEST);
					return false;
				}

				remaining--;

			} while (remaining > 0);

			return true;
		}
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
		sb.append(" ");
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

	protected boolean getCache() {
		return false;
	}

	protected HttpExchange getHttpExchange() {
		return exchange;
	}

	protected InputStream getRequestBody() {
		return exchange.getRequestBody();
	}

	protected String getRawPath() {
		return exchange.getRequestURI().getRawPath();
	}

	protected String[] tokenizeUrl() {

		final String path = exchange.getHttpContext().getPath();
		final String token = exchange.getRequestURI().getPath()
				.substring(path.length());
		return token.split("/");
	}

	protected String getHostName() {
		return exchange.getRemoteAddress().getHostName();
	}

	protected void statistics(final String requested) {
		final String referer = exchange.getRequestHeaders().getFirst(
				AbstractResponse.REQUEST_HEADER_REFERER);

		final String ip = exchange.getRemoteAddress().getAddress()
				.getHostAddress();

		final Calendar calendar = Calendar.getInstance();

		statistics.add(ip, requested, referer, calendar.getTime());
	}
}
