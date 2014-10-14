package pt.go2.application;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;

import pt.go2.fileio.Configuration;
import pt.go2.response.AbstractResponse;
import pt.go2.response.RedirectResponse;
import pt.go2.storage.HashKey;
import pt.go2.storage.Uri;

/**
 * Handles server requests
 */
class StaticPages extends RequestHandler {

	static final Logger logger = LogManager.getLogger(StaticPages.class);

	final Calendar calendar = Calendar.getInstance();
	
	/**
	 * C'tor
	 * 
	 * @param config
	 * @param vfs
	 * @param statistics 
	 * @throws IOException
	 */
	public StaticPages(final Configuration config, final Resources vfs,final BufferedWriter accessLog) {
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
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse exchange)
			throws IOException, ServletException {

        baseRequest.setHandled(true);		
		
		// we need a host header to continue

		final String host = request.getHeader(AbstractResponse.REQUEST_HEADER_HOST);
		
		if (host.isEmpty()) {
			reply(request, exchange, vfs.get(Resources.Error.BAD_REQUEST), false);
			return;
		}

		// redirect to out domain if host header is not correct

		if (config.ENFORCE_DOMAIN != null && !config.ENFORCE_DOMAIN.isEmpty()
				&& !host.startsWith(config.ENFORCE_DOMAIN)) {

			reply(request, exchange, vfs.get(Resources.Error.REJECT_SUBDOMAIN),
					false);

			logger.error("Wrong host: " + host);
			return;
		}

		final String requested = getRequestedFilename(request.getRequestURI());

		if (requested.length() == 6) {

			final Uri uri = vfs.get(new HashKey(requested));

			if (uri == null) {
				reply(request, exchange, vfs.get(Resources.Error.PAGE_NOT_FOUND), true);
				return;
			}

			reply(request, exchange, new RedirectResponse(uri.toString(), 301), true);
			return;
		}

		AbstractResponse response;

		if (requested.equals("/") && config.PUBLIC != null) {
			response = vfs.get(config.PUBLIC_ROOT);
		} else {
			response = vfs.get(requested);
		}

		if (response == null)
			response = vfs.get(Resources.Error.PAGE_NOT_FOUND);

		reply(request, exchange, response, true);
	}

	/**
	 * Parse requested filename from URI
	 * 
	 * @param path
	 * 
	 * @return Requested filename
	 */
	private String getRequestedFilename(final String path) {

		// split into tokens

		if (path.equals("/")) {
			return path;
		}

		final int idx = path.indexOf('/', 1);

		return idx == -1 ? path.substring(1) : path.substring(1, idx);
	}
}
