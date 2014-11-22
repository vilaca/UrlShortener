package pt.go2.fileio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;

import pt.go2.response.AbstractResponse;
import pt.go2.response.GenericResponse;

public class ErrorPages {

	private static final int BUFFER_SIZE = 4096;
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Canned responses for errors
	 */
	public enum Error {
		PAGE_NOT_FOUND, BAD_REQUEST, PHISHING, MALWARE
	}

	private final Map<Error, AbstractResponse> errors = new EnumMap<>(Error.class);

	/**
	 * Cache error responses
	 * 
	 * @param config
	 * @return
	 * @throws IOException
	 */
	public ErrorPages() throws IOException {

		this.errors.put(Error.BAD_REQUEST, new GenericResponse("Bad request.".getBytes(), HttpStatus.BAD_REQUEST_400,
				AbstractResponse.MIME_TEXT_PLAIN));

		try {
			this.errors.put(Error.PAGE_NOT_FOUND,
					new GenericResponse(read(ErrorPages.class.getResourceAsStream("/404.html")),
							HttpStatus.NOT_FOUND_404, AbstractResponse.MIME_TEXT_HTML));
		} catch (IOException e) {
			LOGGER.fatal("Cannot read 404 page.", e);
			throw e;
		}

		try {
			this.errors.put(Error.PHISHING,
					new GenericResponse(read(ErrorPages.class.getResourceAsStream("/404-phishing.html")),
							HttpStatus.NOT_FOUND_404, AbstractResponse.MIME_TEXT_HTML));
		} catch (IOException e) {
			LOGGER.fatal("Cannot read 404-phishing page.", e);
			throw e;
		}

		try {
			this.errors.put(Error.MALWARE,
					new GenericResponse(read(ErrorPages.class.getResourceAsStream("/404-malware.html")),
							HttpStatus.NOT_FOUND_404, AbstractResponse.MIME_TEXT_HTML));
		} catch (IOException e) {
			LOGGER.fatal("Cannot read 404-malware page.", e);
			throw e;
		}
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

	private byte[] read(InputStream is) throws IOException {

		final byte[] buffer = new byte[BUFFER_SIZE];
		int read;

		final ByteArrayOutputStream output = new ByteArrayOutputStream();

		while ((read = is.read(buffer)) != -1) {
			output.write(buffer, 0, read);
		}

		return output.toByteArray();
	}
}
