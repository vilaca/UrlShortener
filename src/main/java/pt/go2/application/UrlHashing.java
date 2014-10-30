package pt.go2.application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pt.go2.application.ErrorPages.Error;
import pt.go2.fileio.Configuration;
import pt.go2.response.HtmlResponse;
import pt.go2.response.ProcessingResponse;
import pt.go2.storage.KeyValueStore;
import pt.go2.storage.Uri;
import pt.go2.storage.Uri.Health;

class UrlHashing extends RequestHandler {

	final KeyValueStore ks;
	final UrlHealth health;

	public UrlHashing(Configuration config, BufferedWriter accessLog,
			ErrorPages errors, KeyValueStore ks, UrlHealth health) {

		super(config, accessLog, errors);

		this.ks = ks;
		this.health = health;
	}

	/**
	 * Handle shortening of Urls.
	 * 
	 * If Url already exists return hash. If Url wasn't hashed before generate
	 * hash and add it to value store
	 */
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response) {

		final Uri uri = urltoHash(request, response);

		if (uri == null)
			return;

		// try to find hash for url is ks

		final Uri stored = ks.find(uri);

		if (stored == null) {

			// hash not found, add new

			ks.add(uri);
			reply(request, response, new ProcessingResponse(), false);
			health.test(uri);
			return;
		}

		if (uri.health() == Health.OK) {
			reply(request, response, new HtmlResponse(ks.get(stored)), false);
			return;
		}

		if (uri.health() == Health.UNKNOWN) {
			reply(request, response, new ProcessingResponse(), false);
			return;

		}
	}

	private Uri urltoHash(HttpServletRequest request,
			HttpServletResponse response) {
		try (final InputStream is = request.getInputStream();
				final InputStreamReader sr = new InputStreamReader(is);
				final BufferedReader br = new BufferedReader(sr);) {

			// read body content

			final String postBody = br.readLine();

			if (postBody == null) {
				reply(request, response, Error.BAD_REQUEST, false);
				return null;
			}

			// format for form content is 'fieldname=value'

			final int idx = postBody.indexOf('=') + 1;

			if (idx == -1 || postBody.length() - idx < 3) {
				reply(request, response, Error.BAD_REQUEST, false);
				return null;
			}

			// Parse string into Uri

			final Uri uri = Uri.create(postBody.substring(idx), true,
					Health.UNKNOWN);

			if (uri == null) {
				reply(request, response, Error.BAD_REQUEST, false);
			}

			return uri;

		} catch (IOException e) {
			reply(request, response, Error.BAD_REQUEST, false);
		}
		return null;
	}
}
