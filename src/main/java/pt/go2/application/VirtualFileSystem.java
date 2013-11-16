package pt.go2.application;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.daemon.PhishTankInterface;
import pt.go2.daemon.WatchDog;
import pt.go2.daemon.WatchDogTask;
import pt.go2.fileio.Configuration;
import pt.go2.fileio.SmartTagParser;
import pt.go2.keystore.HashKey;
import pt.go2.keystore.KeyValueStore;
import pt.go2.keystore.Uri;
import pt.go2.response.AbstractResponse;
import pt.go2.response.ErrorResponse;
import pt.go2.response.RedirectResponse;
import pt.go2.response.GzipResponse;

/**
 * Virtualize file resources
 * 
 */
class VirtualFileSystem {

	/**
	 * Canned responses for errors
	 */

	enum Error {
		PAGE_NOT_FOUND, REJECT_SUBDOMAIN, BAD_REQUEST, FORBIDDEN_PHISHING
	}

	static final Logger logger = LogManager.getLogger(VirtualFileSystem.class);

	private WatchDog watchdog = new WatchDog();

	// error responses
	private final Map<Error, AbstractResponse> errors = new EnumMap<>(
			Error.class);

	// responses
	private final Map<String, AbstractResponse> pages = new HashMap<>();

	// key = hash / value = uri
	private final KeyValueStore ks;

	private PhishTankInterface pi;

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
			logger.fatal("Could not read backup.");
			return null;
		}

		final VirtualFileSystem vfs = new VirtualFileSystem(ks);

		final PhishTankInterface pi = PhishTankInterface.create(config);

		if (pi == null) {
			logger.warn("Could init PhishTank API Interface.");
		} else {
			vfs.register(pi);
			vfs.set(pi);
		}

		final SmartTagParser fr = new SmartTagParser("/");

		if (!createErrorPages(config, vfs, fr)) {
			logger.fatal("Could not create error pages.");
			return null;
		}

		if (!createEmbeddedPages(config, vfs, fr)) {
			logger.fatal("Could not create embedded pages.");
			return null;
		}

		return vfs;
	}

	private void register(WatchDogTask task) {
		watchdog.register(task, true);
	}

	private void set(PhishTankInterface pi) {
		this.pi = pi;
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
	 * Read pages from JAR
	 * 
	 * @param config
	 * @param vfs
	 * @param fr
	 * @return
	 */
	private static boolean createEmbeddedPages(final Configuration config,
			final VirtualFileSystem vfs, final SmartTagParser fr) {

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

		vfs.put("/", new GzipResponse(index, AbstractResponse.MIME_TEXT_HTML));
		vfs.put("ajax.js", new GzipResponse(ajax,
				AbstractResponse.MIME_APP_JAVASCRIPT));

		vfs.put("robots.txt", new GzipResponse(robots,
				AbstractResponse.MIME_TEXT_PLAIN));

		vfs.put("sitemap.xml", new GzipResponse(map,
				AbstractResponse.MIME_TEXT_XML));

		vfs.put("screen.css", new GzipResponse(css,
				AbstractResponse.MIME_TEXT_CSS));

		if (!config.GOOGLE_VERIFICATION.isEmpty()) {
			vfs.put(config.GOOGLE_VERIFICATION,
					new GzipResponse(
							("google-site-verification: " + config.GOOGLE_VERIFICATION)
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

		// TODO are you sure this is a good message ??
		try {
			vfs.put(Error.FORBIDDEN_PHISHING,
					new ErrorResponse(fr.read("403.html"), 403,
							AbstractResponse.MIME_TEXT_HTML));
		} catch (IOException e) {
			logger.fatal("Cannot read 403 page.");
			return false;
		}

		vfs.put(Error.BAD_REQUEST, new ErrorResponse("Bad request.".getBytes(),
				400, AbstractResponse.MIME_TEXT_PLAIN));

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

	public boolean isBanned(Uri uri) {

		if (uri.getState() != Uri.State.OK) {
			return true;
		}

		return pi.isBanned(uri);
	}

	public byte[] add(Uri uri) {
		return ks.add(uri);
	}

	public Uri get(HashKey haskey) {
		return ks.get(haskey);
	}

	public AbstractResponse get(String requested) {
		return pages.get(requested);
	}
}
