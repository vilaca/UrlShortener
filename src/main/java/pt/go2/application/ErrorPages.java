package pt.go2.application;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.fileio.Configuration;
import pt.go2.fileio.SmartTagParser;
import pt.go2.response.AbstractResponse;
import pt.go2.response.ErrorResponse;

// TODO needs improvement

public class ErrorPages {

	static final Logger logger = LogManager.getLogger();

	/**
	 * Canned responses for errors
	 */
	enum Error {
		PAGE_NOT_FOUND, BAD_REQUEST, FORBIDDEN_PHISHING, FORBIDDEN_MALWARE
	}

	private final Map<Error, AbstractResponse> errors = new EnumMap<>(Error.class);

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
	public ErrorPages(Configuration conf) throws IOException {

		this.errors.put(Error.BAD_REQUEST, new ErrorResponse("Bad request.".getBytes(), 400,
				AbstractResponse.MIME_TEXT_PLAIN));

		try {
			this.errors.put(Error.PAGE_NOT_FOUND,
					new ErrorResponse(SmartTagParser.read(ErrorPages.class.getResourceAsStream("/404.html"), conf),
							404, AbstractResponse.MIME_TEXT_HTML));
		} catch (IOException e) {
			logger.fatal("Cannot read 404 page.", e);
			throw e;
		}

		try {
			this.errors.put(
					Error.FORBIDDEN_PHISHING,
					new ErrorResponse(SmartTagParser.read(ErrorPages.class.getResourceAsStream("/403-phishing.html"),
							conf), 404, AbstractResponse.MIME_TEXT_HTML));
		} catch (IOException e) {
			logger.fatal("Cannot read 403-phishing page.", e);
			throw e;
		}

		try {
			this.errors.put(
					Error.FORBIDDEN_MALWARE,
					new ErrorResponse(SmartTagParser.read(ErrorPages.class.getResourceAsStream("/403-malware.html"),
							conf), 404, AbstractResponse.MIME_TEXT_HTML));
		} catch (IOException e) {
			logger.fatal("Cannot read 403-malware page.", e);
			throw e;
		}
	}
}
