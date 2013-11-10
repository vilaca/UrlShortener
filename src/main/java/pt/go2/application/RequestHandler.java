package pt.go2.application;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import pt.go2.fileio.Configuration;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

class RequestHandler implements HttpHandler {

	private final Configuration config;
	private final BufferedWriter accessLog;
	private final VirtualFileSystem vfs;

	/**
	 * C'tor
	 * 
	 * @param config
	 * @param vfs
	 * @throws IOException
	 */
	public RequestHandler(final Configuration config,
			final VirtualFileSystem vfs, final BufferedWriter accessLog) {

		this.config = config;
		this.vfs = vfs;
		this.accessLog = accessLog;
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

			reply(exchange, vfs.get(VirtualFileSystem.Error.BAD_REQUEST));
			return;
		}

		// redirect to out domain if host header is not correct

		if (!correctHost(request)) {

			reply(exchange, vfs.get(VirtualFileSystem.Error.REJECT_SUBDOMAIN));
			return;
		}

		final String requested = getRequestedFilename(exchange.getRequestURI());

		final AbstractResponse response = vfs.getPage(requested);

		reply(exchange, response);
	}

	/**
	 * Stream Http Response
	 * 
	 * @param exchange
	 * @param response
	 * @throws IOException
	 */
	private void reply(final HttpExchange exchange,
			final AbstractResponse response) throws IOException {

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
				"Carapau de corrida" + config.VERSION);

		headers.set(AbstractResponse.RESPONSE_HEADER_CONTENT_TYPE,
				response.getMimeType());

		headers.set(AbstractResponse.RESPONSE_HEADER_CACHE_CONTROL,
				"max-age=" + 60 * 60 * 24);

		if (response.isZipped()) {
			headers.set(AbstractResponse.RESPONSE_HEADER_CONTENT_ENCODING,
					"gzip");
		}
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
	 * Is the request for this domain?
	 * 
	 * (Used to redirect from www.go2.pt to go2.pt)
	 * 
	 * @param headers
	 * @return
	 */
	private boolean correctHost(final Headers headers) {
		return headers.getFirst(AbstractResponse.REQUEST_HEADER_HOST)
				.startsWith(config.ENFORCE_DOMAIN);
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

	/**
	 * Access log output
	 * 
	 * @param params
	 * @param response
	 */
	void printLogMessage(final HttpExchange params,
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
		final String referer = headers.getFirst("Referer");
		final String agent = headers.getFirst("User-Agent");

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
