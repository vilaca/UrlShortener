package pt.go2.application;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.daemon.PhishTankInterface;
import pt.go2.daemon.WatchDog;
import pt.go2.fileio.Configuration;
import pt.go2.fileio.EmbeddedFiles;
import pt.go2.fileio.FileSystemInterface;
import pt.go2.fileio.LocalFiles;
import pt.go2.fileio.SmartTagParser;
import pt.go2.keystore.HashKey;
import pt.go2.keystore.KeyValueStore;
import pt.go2.keystore.Uri;
import pt.go2.response.AbstractResponse;
import pt.go2.response.SimpleResponse;
import pt.go2.response.RedirectResponse;

/**
 * Virtualized file resources
 * 
 */
// TODO move error pages out ?
public class Resources {

	/**
	 * Canned responses for errors
	 */

	public enum Error {
		PAGE_NOT_FOUND, REJECT_SUBDOMAIN, BAD_REQUEST, FORBIDDEN_PHISHING, FORBIDDEN_PHISHING_AJAX, FORBIDDEN_USER_ALREADY_EXISTS, ERROR_CREATING_USER, ERROR_VALIDATING_USER, USER_VALIDATED, FORBIDDEN, USER_LOGIN_SUCESSFUL
	}

	static final Logger logger = LogManager.getLogger(Resources.class);

	private WatchDog watchdog = new WatchDog();

	// error responses
	private final Map<Error, AbstractResponse> errors = new EnumMap<>(
			Error.class);

	// responses
	private FileSystemInterface pages;

	// key = hash / value = uri
	private KeyValueStore ks;

	private PhishTankInterface pi;

	/**
	 * C'tor
	 * 
	 * @param ks
	 */
	public Resources() {
	}

	public boolean start(final Configuration config) {

		try {
			this.ks = new KeyValueStore(config.DATABASE_FOLDER);
		} catch (IOException e) {
			logger.fatal("Could not read backup.");
			return false;
		}

		if (!createErrorPages(config)) {
			logger.fatal("Could not create error pages.");
			return false;
		}

		try {
			if (config.PUBLIC == null) {

				pages = new EmbeddedFiles(config);
			} else {

				pages = new LocalFiles(config);
			}

		} catch (IOException e) {
			logger.error("Could load public files.");
			return false;
		}

		pages.start();

		this.pi = PhishTankInterface.create(config);

		if (pi == null) {
			logger.warn("Could init PhishTank API Interface.");
		} else {
			watchdog.register(pi, true);
			watchdog.start(config.WATCHDOG_INTERVAL);
		}

		return true;
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
	 * Cache error responses
	 * 
	 * @param config
	 * @return
	 */
	private boolean createErrorPages(final Configuration config) {

		try {
			this.errors.put(
					Error.PAGE_NOT_FOUND,
					new SimpleResponse(SmartTagParser.read(Resources.class
							.getResourceAsStream("/404.html")), 404,
							AbstractResponse.MIME_TEXT_HTML));
		} catch (IOException e) {
			logger.fatal("Cannot read 404 page.");
			return false;
		}

		try {
			this.errors.put(
					Error.FORBIDDEN_PHISHING,
					new SimpleResponse(SmartTagParser.read(Resources.class
							.getResourceAsStream("/403.html")), 403,
							AbstractResponse.MIME_TEXT_HTML));
		} catch (IOException e) {
			logger.fatal("Cannot read 403 page.");
			return false;
		}

		this.errors.put(Error.FORBIDDEN_PHISHING_AJAX, new SimpleResponse(
				"Forbidden".getBytes(), 403, AbstractResponse.MIME_TEXT_PLAIN));

		this.errors.put(Error.FORBIDDEN_USER_ALREADY_EXISTS,
				new SimpleResponse("User already exists.".getBytes(), 403,
						AbstractResponse.MIME_TEXT_PLAIN));

		this.errors.put(Error.FORBIDDEN, new SimpleResponse(
				"Wrong username or password.".getBytes(), 403,
				AbstractResponse.MIME_TEXT_PLAIN));

		this.errors.put(Error.ERROR_CREATING_USER, new SimpleResponse(
				"Error creating user.".getBytes(), 500,
				AbstractResponse.MIME_TEXT_PLAIN));

		this.errors.put(Error.BAD_REQUEST,
				new SimpleResponse("Bad request.".getBytes(), 400,
						AbstractResponse.MIME_TEXT_PLAIN));

		this.errors.put(Error.ERROR_VALIDATING_USER, new SimpleResponse(
				"User can't be validated".getBytes(), 404,
				AbstractResponse.MIME_TEXT_PLAIN));

		this.errors.put(Error.USER_VALIDATED, new SimpleResponse(
				"User validated. Please login to continue.".getBytes(), 200,
				AbstractResponse.MIME_TEXT_PLAIN));

		this.errors.put(Error.USER_LOGIN_SUCESSFUL, new SimpleResponse(
				"Login OK!.".getBytes(), 200,
				AbstractResponse.MIME_TEXT_PLAIN));

		// redirect to domain if a sub-domain is being used

		this.errors.put(Error.REJECT_SUBDOMAIN, new RedirectResponse("http://"
				+ config.ENFORCE_DOMAIN, 301));

		return true;
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
		return pages.getFile(requested);
	}

	public List<String> browse() {
		return pages.browse();
	}
}
