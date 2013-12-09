package pt.go2.api;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.application.Resources;
import pt.go2.fileio.Configuration;
import pt.go2.fileio.SmartTagParser;
import pt.go2.response.AbstractResponse;
import pt.go2.response.RedirectResponse;
import pt.go2.response.SimpleResponse;

public class ErrorMessages {

	// TODO some are no longer being used
	public enum Error {
		PAGE_NOT_FOUND, REJECT_SUBDOMAIN, BAD_REQUEST, FORBIDDEN_PHISHING, FORBIDDEN_PHISHING_AJAX, FORBIDDEN_USER_ALREADY_EXISTS, ERROR_CREATING_USER, ERROR_VALIDATING_USER, USER_VALIDATED, FORBIDDEN, USER_LOGIN_SUCESSFUL, HASH_NOT_FOUND
	}

	private static final Logger logger = LogManager.getLogger(Resources.class);

	private final Map<Error, AbstractResponse> errors;

	public ErrorMessages(Map<Error, AbstractResponse> errors) {
		this.errors = errors;
	}

	public AbstractResponse get(Error e) {
		return errors.get(e);
	}

	public static ErrorMessages create(final Configuration config) {
		final Map<Error, AbstractResponse> errors = new EnumMap<>(Error.class);
		try {
			errors.put(
					Error.PAGE_NOT_FOUND,
					new SimpleResponse(SmartTagParser.read(Resources.class
							.getResourceAsStream("/404.html")), 404,
							AbstractResponse.MIME_TEXT_HTML));
		} catch (IOException e) {
			logger.fatal("Cannot read 404 page.");
			return null;
		}

		try {
			errors.put(
					Error.FORBIDDEN_PHISHING,
					new SimpleResponse(SmartTagParser.read(Resources.class
							.getResourceAsStream("/403.html")), 403,
							AbstractResponse.MIME_TEXT_HTML));
		} catch (IOException e) {
			logger.fatal("Cannot read 403 page.");
			return null;
		}

		errors.put(Error.FORBIDDEN_PHISHING_AJAX, new SimpleResponse(
				"Forbidden".getBytes(), 403, AbstractResponse.MIME_TEXT_PLAIN));

		errors.put(Error.FORBIDDEN_USER_ALREADY_EXISTS, new SimpleResponse(
				"User already exists.".getBytes(), 403,
				AbstractResponse.MIME_TEXT_PLAIN));

		errors.put(Error.FORBIDDEN, new SimpleResponse(
				"Wrong username or password.".getBytes(), 403,
				AbstractResponse.MIME_TEXT_PLAIN));

		errors.put(Error.ERROR_CREATING_USER, new SimpleResponse(
				"Error creating user.".getBytes(), 500,
				AbstractResponse.MIME_TEXT_PLAIN));

		errors.put(Error.BAD_REQUEST,
				new SimpleResponse("Bad request.".getBytes(), 400,
						AbstractResponse.MIME_TEXT_PLAIN));

		errors.put(Error.ERROR_VALIDATING_USER, new SimpleResponse(
				"User can't be validated".getBytes(), 404,
				AbstractResponse.MIME_TEXT_PLAIN));

		errors.put(Error.USER_VALIDATED, new SimpleResponse(
				"User validated. Please login to continue.".getBytes(), 200,
				AbstractResponse.MIME_TEXT_PLAIN));

		errors.put(Error.HASH_NOT_FOUND,
				new SimpleResponse("Not found!.".getBytes(), 404,
						AbstractResponse.MIME_TEXT_PLAIN));

		errors.put(Error.USER_LOGIN_SUCESSFUL,
				new SimpleResponse("Login OK!.".getBytes(), 200,
						AbstractResponse.MIME_TEXT_PLAIN));

		// redirect to domain if a sub-domain is being used

		errors.put(Error.REJECT_SUBDOMAIN, new RedirectResponse("http://"
				+ config.ENFORCE_DOMAIN, 301));

		return new ErrorMessages(errors);
	}
}
