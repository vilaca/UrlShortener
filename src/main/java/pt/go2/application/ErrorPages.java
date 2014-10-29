package pt.go2.application;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.fileio.SmartTagParser;
import pt.go2.response.AbstractResponse;
import pt.go2.response.ErrorResponse;

// TODO better name
public class ErrorPages {

	static final Logger logger = LogManager.getLogger();

	/**
	 * Canned responses for errors
	 */
	enum Error {
		PAGE_NOT_FOUND, BAD_REQUEST, FORBIDDEN_PHISHING, FORBIDDEN_PHISHING_AJAX
	}

	private final Map<Error, AbstractResponse> errors = new EnumMap<>(
			Error.class);

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
	 * @throws IOException
	 */
	public ErrorPages() throws IOException {

		try {
			this.errors.put(
					Error.PAGE_NOT_FOUND,
					new ErrorResponse(SmartTagParser.read(Resources.class
							.getResourceAsStream("/404.html")), 404,
							AbstractResponse.MIME_TEXT_HTML));
		} catch (IOException e) {
			logger.fatal("Cannot read 404 page.", e);
			throw e;
		}

/*		try {
			this.errors.put(
					Error.FORBIDDEN_PHISHING,
					new ErrorResponse(SmartTagParser.read(Resources.class
							.getResourceAsStream("/403.html")), 403,
							AbstractResponse.MIME_TEXT_HTML));
		} catch (IOException e) {
			logger.fatal("Cannot read 403 page.", e);
			throw e;
		}

		this.errors.put(Error.FORBIDDEN_PHISHING_AJAX, new ErrorResponse(
				"Forbidden".getBytes(), 403, AbstractResponse.MIME_TEXT_PLAIN));
*/
		this.errors.put(Error.BAD_REQUEST,
				new ErrorResponse("Bad request.".getBytes(), 400,
						AbstractResponse.MIME_TEXT_PLAIN));
	}
}
