package pt.go2.application;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.fileio.Configuration;
import pt.go2.fileio.SmartTagParser;
import pt.go2.keystore.KeyValueStore;
import pt.go2.keystore.Uri;

class VirtualFileSystem {

	/**
	 * Canned responses for errors
	 */
	public enum Error {
		PAGE_NOT_FOUND, REJECT_SUBDOMAIN, BAD_REQUEST
	}

	static final Logger logger = LogManager.getLogger(VirtualFileSystem.class);

	// error responses
	private final Map<Error, AbstractResponse> errors = new EnumMap<>(
			Error.class);

	// responses
	private final Map<String, AbstractResponse> pages = new HashMap<>();

	// key = hash / value = uri
	private final KeyValueStore ks;

	/**
	 * C'tor
	 * 
	 * @param ks
	 */
	private VirtualFileSystem(KeyValueStore ks) {
		this.ks = ks;
	}

	/**
	 * Factory method
	 * 
	 * @param config
	 * @return
	 */
	public static VirtualFileSystem create(final Configuration config) {

		KeyValueStore ks;
		try {
			ks = new KeyValueStore(config.DATABASE_FOLDER);
		} catch (IOException e) {
			return null;
		}

		final VirtualFileSystem vfs = new VirtualFileSystem(ks);

		final SmartTagParser fr = new SmartTagParser("/");

		if (!createErrorPages(config, vfs, fr)) {
			return null;
		}

		if (!createEmbeddedPages(config, vfs, fr, ks)) {
			return null;
		}

		return vfs;
	}

	/**
	 * Return error response
	 * 
	 * @param badRequest
	 * @return
	 */
	public AbstractResponse get(Error error) {
		return errors.get(error);
	}

	/**
	 * Get page or hash
	 * 
	 * @param requested
	 * @return
	 */
	public AbstractResponse getPage(String requested) {

		if (requested.length() == 6) {

			final Uri uri = ks.get(requested);

			if (uri == null) {
				return errors.get(Error.PAGE_NOT_FOUND);
			}

			return new RedirectResponse(uri.toString(), 301);
		}

		final AbstractResponse response = pages.get(requested);

		return response != null ? response : errors.get(Error.PAGE_NOT_FOUND);
	}

	/**
	 * Read pages from JAR
	 * 
	 * @param config
	 * @param vfs
	 * @param fr
	 * @param ks
	 * @return
	 */
	private static boolean createEmbeddedPages(final Configuration config,
			final VirtualFileSystem vfs, final SmartTagParser fr,
			final KeyValueStore ks) {

		final byte[] index, ajax, robots, map, css;

		try {
			index = fr.read("index.html");
			ajax = fr.read("ajax.js");
			robots = fr.read("robots.txt");
			map = fr.read("map.txt");
			css = fr.read("screen.css");
		} catch (IOException e) {
			logger.fatal("Can't read embedded page.");
			return false;
		}

		vfs.put("/", new StaticResponse(index, AbstractResponse.MIME_TEXT_HTML));
		vfs.put("ajax.js", new StaticResponse(ajax,
				AbstractResponse.MIME_APP_JAVASCRIPT));

		vfs.put("robots.txt", new StaticResponse(robots,
				AbstractResponse.MIME_TEXT_PLAIN));

		vfs.put("sitemap.xml", new StaticResponse(map,
				AbstractResponse.MIME_TEXT_XML));

		vfs.put("screen.css", new StaticResponse(css,
				AbstractResponse.MIME_TEXT_CSS));

		vfs.put("new", new HashResponse(ks));

		if (!config.GOOGLE_VALIDATION.isEmpty()) {
			vfs.put(config.GOOGLE_VALIDATION,
					new StaticResponse(
							("google-site-verification: " + config.GOOGLE_VALIDATION)
									.getBytes(),
							AbstractResponse.MIME_TEXT_PLAIN));
		}

		return true;
	}

	/**
	 * Cache error responses
	 * 
	 * @param config
	 * @param vfs
	 * @param fr
	 * @return
	 */
	private static boolean createErrorPages(final Configuration config,
			final VirtualFileSystem vfs, final SmartTagParser fr) {

		try {
			vfs.put(Error.PAGE_NOT_FOUND, new ErrorResponse(
					fr.read("404.html"), 404, AbstractResponse.MIME_TEXT_HTML));
		} catch (IOException e) {
			logger.fatal("Cannot read 404 page.");
			return false;
		}

		vfs.put(Error.PAGE_NOT_FOUND,
				new ErrorResponse("Bad request.".getBytes(), 400,
						AbstractResponse.MIME_TEXT_PLAIN));

		// redirect to domain if a sub-domain is being used

		vfs.put(Error.REJECT_SUBDOMAIN, new RedirectResponse("http://"
				+ config.ENFORCE_DOMAIN, 301));

		return true;
	}

	/**
	 * Add response
	 * 
	 * @param string
	 * @param abstractResponse
	 */
	private void put(String string, AbstractResponse abstractResponse) {
		pages.put(string, abstractResponse);
	}

	/**
	 * Add error response
	 * 
	 * @param response
	 * @param page
	 */
	private void put(final Error response, final AbstractResponse page) {
		errors.put(response, page);
	}
}
