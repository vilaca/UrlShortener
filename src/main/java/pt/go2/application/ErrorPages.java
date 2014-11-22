package pt.go2.application;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;

import pt.go2.fileio.Configuration;
import pt.go2.fileio.SmartTagParser;
import pt.go2.response.AbstractResponse;
import pt.go2.response.GenericResponse;

// TODO needs improvement

public class ErrorPages {

	private static final Logger LOGGER = LogManager.getLogger();

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

		this.errors.put(Error.BAD_REQUEST, new GenericResponse("Bad request.".getBytes(), HttpStatus.BAD_REQUEST_400,
				AbstractResponse.MIME_TEXT_PLAIN));

		try {
			this.errors.put(Error.PAGE_NOT_FOUND,
					new GenericResponse(SmartTagParser.read(ErrorPages.class.getResourceAsStream("/404.html"), conf),
							HttpStatus.NOT_FOUND_404, AbstractResponse.MIME_TEXT_HTML));
		} catch (IOException e) {
			LOGGER.fatal("Cannot read 404 page.", e);
			throw e;
		}

		try {
			this.errors.put(
					Error.FORBIDDEN_PHISHING,
					new GenericResponse(SmartTagParser.read(ErrorPages.class.getResourceAsStream("/403-phishing.html"),
							conf), HttpStatus.NOT_FOUND_404, AbstractResponse.MIME_TEXT_HTML));
		} catch (IOException e) {
			LOGGER.fatal("Cannot read 403-phishing page.", e);
			throw e;
		}

		try {
			this.errors.put(
					Error.FORBIDDEN_MALWARE,
					new GenericResponse(SmartTagParser.read(ErrorPages.class.getResourceAsStream("/403-malware.html"),
							conf), HttpStatus.NOT_FOUND_404, AbstractResponse.MIME_TEXT_HTML));
		} catch (IOException e) {
			LOGGER.fatal("Cannot read 403-malware page.", e);
			throw e;
		}
	}
}
