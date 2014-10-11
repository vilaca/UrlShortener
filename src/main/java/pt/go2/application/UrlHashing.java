package pt.go2.application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;

import pt.go2.fileio.Configuration;
import pt.go2.keystore.Uri;
import pt.go2.response.HtmlResponse;

class UrlHashing extends RequestHandler {

	static private final Logger logger = LogManager.getLogger(UrlHashing.class);

	private final Resources vfs;

	/**
	 * C'tor
	 * 
	 * @param config
	 * 
	 * @param config
	 * @param vfs
	 * @param accessLog
	 * @throws IOException
	 */
	public UrlHashing(Configuration config, final Resources vfs,
			BufferedWriter accessLog) {
		
		super(config, vfs, accessLog);
		this.vfs = vfs;
	}

	/**
	 * Handle shortening of Urls.
	 * 
	 * If Url already exists return hash. If Url wasn't hashed before generate
	 * hash and add it to value store
	 */
	
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		
		try (final InputStream is = request.getInputStream();
				final InputStreamReader sr = new InputStreamReader(is);
				final BufferedReader br = new BufferedReader(sr);) {

			// read body content

			final String postBody = br.readLine();

			if (postBody == null) {
				reply(request, response, vfs.get(Resources.Error.BAD_REQUEST),
						false);
				return;
			}

			// format for form content is 'fieldname=value'

			final int idx = postBody.indexOf('=') + 1;

			if (idx == -1 || postBody.length() - idx < 3) {
				reply(request, response, vfs.get(Resources.Error.BAD_REQUEST),
						false);
				return;
			}

			// Parse string into Uri

			final Uri uri = Uri.create(postBody.substring(idx), true);

			if (uri == null) {
				reply(request, response, vfs.get(Resources.Error.BAD_REQUEST),
						false);
				return;
			}

			// Refuse banned

			if (vfs.isBanned(uri)) {
				logger.warn("banned: " + uri + " - "
						+ request.getRemoteAddr());
				reply(request, response,
						vfs.get(Resources.Error.FORBIDDEN_PHISHING_AJAX),
						false);
				return;
			}

			// hash Uri

			final byte[] hashedUri = vfs.add(uri);

			if (hashedUri.length == 0) {
				reply(request, response, vfs.get(Resources.Error.BAD_REQUEST),
						false);
				return;
			}

			reply(request, response, new HtmlResponse(hashedUri), false);

		} catch (IOException e) {
			reply(request, response, vfs.get(Resources.Error.BAD_REQUEST), false);
			return;
		}
	}
}
