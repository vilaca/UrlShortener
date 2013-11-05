package pt.go2.application;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import pt.go2.fileio.Configuration;
import pt.go2.fileio.SmartTagParser;
import pt.go2.keystore.KeyValueStore;
import pt.go2.pagelets.PageLet;
import pt.go2.pagelets.RedirectPageLet;
import pt.go2.pagelets.ShortenerPageLet;
import pt.go2.pagelets.StaticPageLetBuilder;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

class RequestHandler implements HttpHandler, Closeable {

	// Headers set in response
	private static final String RESPONSE_HEADER_CACHE_CONTROL = "Cache-Control";
	private static final String RESPONSE_HEADER_CONTENT_ENCODING = "Content-Encoding";
	private static final String RESPONSE_HEADER_CONTENT_TYPE = "Content-Type";
	private static final String RESPONSE_HEADER_SERVER = "Server";
	
	// Miscellaneous parameters
	private static final String CARAPAU_DE_CORRIDA = "Carapau de corrida ";
	
	private final Map<String, PageLet> pages;
	private final Map<ServerResponse, PageLet> special;
	private final Configuration config;
	private final KeyValueStore ks;
	private final BufferedWriter accessLog;

	enum ServerResponse {
		PAGE_NOT_FOUND, REJECT_SUBDOMAIN, BAD_REQUEST
	}
	
	/**
	 * C'tor 
	 * 
	 * @param config
	 * @throws IOException
	 */
	public RequestHandler(final Configuration config, final BufferedWriter accessLog) throws IOException {

		this.config = config;
		this.accessLog = accessLog;

		// restore URI/hash mappings data
		this.ks = new KeyValueStore(config.DATABASE_FOLDER, config.REDIRECT);

		// map static pages to URI part
		this.pages = Collections.unmodifiableMap(generatePagesDecoder());		

		// map HTTP error responses
		this.special = Collections.unmodifiableMap(generateSpecialDecoder());		
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

		final PageLet resource = getPageContents(exchange);

		exchange.getResponseHeaders().set(RESPONSE_HEADER_SERVER,
				CARAPAU_DE_CORRIDA + config.VERSION);

		final HttpResponse response = resource.getPageLet(exchange);

		final Headers headers = exchange.getResponseHeaders();

		if (response.isZipped()) {
			headers.set(RESPONSE_HEADER_CONTENT_ENCODING, "gzip");
		}

		headers.set(RESPONSE_HEADER_CONTENT_TYPE, response.getMimeType());

		// cache for a whole day
		headers.set(RESPONSE_HEADER_CACHE_CONTROL, "max-age=" + 60 * 60 * 24);

		exchange.sendResponseHeaders(response.getHttpErrorCode(),
				response.getSize());

		final OutputStream os = exchange.getResponseBody();

		os.write(response.getBody());

		os.flush();
		os.close();

		printLogMessage(exchange, response);
	}

	/**
	 * Resolve URI to correct page/resource or use 404
	 * 
	 * @param exchange
	 *            .getRequestURI()
	 * @return
	 */
	private PageLet getPageContents(final HttpExchange exchange) {

		final Headers headers = exchange.getRequestHeaders();

		if (!validRequest(headers)) {
			return special.get(ServerResponse.REJECT_SUBDOMAIN);
		}

		final String filename = getRequestedFilename(exchange.getRequestURI());

		if (filename.equals("/") && config.ENFORCE_DOMAIN != null) {

			if (!correctHost(headers))
				return special.get(ServerResponse.REJECT_SUBDOMAIN);
		}

		final PageLet page;

		if (filename.length() == 6) {
			page = ks.get(filename);
		} else {
			page = pages.get(filename);
		}

		return page != null ? page : special.get(ServerResponse.PAGE_NOT_FOUND);
	}

	/**
	 * Server needs a Host header
	 * 
	 * @param headers
	 * @return
	 */
	private boolean validRequest(final Headers headers) {
		return headers.get("Host").size() > 0;
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
		return headers.getFirst("Host").startsWith(config.ENFORCE_DOMAIN);
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

		final String[] tokens = request.getRawPath().split("/");

		if (tokens.length > 0) {
			return tokens[1];
		}

		// empty URI (no tokens) means front page

		return "/";
	}

	/**
	 * URL to Pagelet Multiplexer
	 * 
	 * @return
	 * @throws IOException
	 */
	private Map<String, PageLet> generatePagesDecoder()
			throws IOException {

		final SmartTagParser fr = new SmartTagParser("/");
		final Map<String, PageLet> pages = new HashMap<>();

		pages.put("/", 
				new StaticPageLetBuilder()
						.setContent(fr.read("index.html")).zip().build());

		pages.put("ajax.js",
				new StaticPageLetBuilder()
						.setContent(fr.read("ajax.js"))
						.setMimeType("application/javascript").zip().build());

		pages.put("robots.txt",
				new StaticPageLetBuilder()
						.setContent(fr.read("robots.txt"))
						.setMimeType("text/plain").zip().build());

		pages.put("sitemap.xml",
				new StaticPageLetBuilder()
						.setContent(fr.read("map.txt"))
						.setMimeType("text/xml").zip().build());

		pages.put("screen.css",
				new StaticPageLetBuilder()
						.setContent(fr.read("screen.css"))
						.setMimeType("text/css").zip().build());

		// dynamic pages
		pages.put("new", new ShortenerPageLet(ks));

		// google webmaster tools site verification
		
		if ( !config.GOOGLE_VALIDATION.isEmpty())
		{
			pages.put(config.GOOGLE_VALIDATION,
					new StaticPageLetBuilder()
							.setContent("google-site-verification: " + config.GOOGLE_VALIDATION)
							.setResponseCode(200).zip().build());
		}
		
		return pages;
	}

	/**
	 * Create pagelets for http status codes 
	 * 
	 * @return
	 * @throws IOException 
	 */
	private Map<ServerResponse, PageLet> generateSpecialDecoder() throws IOException {

		final SmartTagParser fr = new SmartTagParser("/");
		final Map<ServerResponse, PageLet> response = new EnumMap<>(
				ServerResponse.class);

		// page not found

		response.put(ServerResponse.PAGE_NOT_FOUND, new StaticPageLetBuilder()
				.setContent(fr.read("404.html")).setResponseCode(404).zip()
				.build());

		// redirect to domain if a sub-domain is being used

		response.put(ServerResponse.REJECT_SUBDOMAIN, new RedirectPageLet(301,
				"http://" + config.ENFORCE_DOMAIN));

		// bad request

		response.put(ServerResponse.BAD_REQUEST, new PageLet() {

			@Override
			public HttpResponse getPageLet(HttpExchange exchange)
					throws IOException {
				return HttpResponse.createBadRequest();
			}
		});

		return response;
	}

	
	/**
	 * Access log output
	 * 
	 * @param params
	 * @param response
	 */
	void printLogMessage(final HttpExchange params,
			final HttpResponse response) {

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
		sb.append(response.getHttpErrorCode());
		sb.append(" ");
		sb.append(response.getSize());
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

	@Override
	public void close() throws IOException {
		ks.close();
	}	
}
