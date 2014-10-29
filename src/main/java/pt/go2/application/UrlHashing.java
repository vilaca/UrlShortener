package pt.go2.application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import pt.go2.application.ErrorPages.Error;
import pt.go2.fileio.Configuration;
import pt.go2.response.HtmlResponse;
import pt.go2.response.ProcessingResponse;
import pt.go2.storage.KeyValueStore;
import pt.go2.storage.Uri;
import pt.go2.storage.Uri.Health;

class UrlHashing extends RequestHandler {

	final KeyValueStore ks;

	public UrlHashing(Configuration config, BufferedWriter accessLog,
			ErrorPages errors, KeyValueStore ks) {

		super(config, accessLog, errors);

		this.ks = ks;
	}

	/**
	 * Handle shortening of Urls.
	 * 
	 * If Url already exists return hash. If Url wasn't hashed before generate
	 * hash and add it to value store
	 */

	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		baseRequest.setHandled(true);

		try (final InputStream is = request.getInputStream();
				final InputStreamReader sr = new InputStreamReader(is);
				final BufferedReader br = new BufferedReader(sr);) {

			// read body content

			final String postBody = br.readLine();

			if (postBody == null) {
				reply(request, response, Error.BAD_REQUEST, false);
				return;
			}

			// format for form content is 'fieldname=value'

			final int idx = postBody.indexOf('=') + 1;

			if (idx == -1 || postBody.length() - idx < 3) {
				reply(request, response, Error.BAD_REQUEST, false);
				return;
			}

			// Parse string into Uri

			final Uri uri = Uri.create(postBody.substring(idx), true,
					Health.UNKNOWN);

			if (uri == null) {
				reply(request, response, Error.BAD_REQUEST, false);
				return;
			}

			final Uri stored = ks.find(uri);

			if (stored != null) {
				switch (stored.health()) {
				case OK:
					reply(request, response, new HtmlResponse(ks.get(stored)),
							false);
					break;
				case BAD:
				case MALWARE:
				case PHISHING:
				case REDIRECT:
					// TODO
					break;
				case UNKNOWN:
					reply(request, response, new ProcessingResponse(), false);
					ks.add(uri);
					break;
				default:
					break;
				}
			}

			// hash Uri

			final byte[] hashedUri = ks.add(uri);

			if (hashedUri.length == 0) {
				reply(request, response, Error.BAD_REQUEST, false);
				return;
			}

			// Refuse banned

			//vfs.get().test(uri);

			/*
			 * if (vfs.isBanned(uri)) { logger.warn("banned: " + uri + " - " +
			 * request.getRemoteAddr()); reply(request, response,
			 * vfs.get(Resources.Error.FORBIDDEN_PHISHING_AJAX), false); return;
			 * }
			 */

		} catch (IOException e) {
			reply(request, response, Error.BAD_REQUEST, false);
			return;
		}
	}
}
