package pt.go2.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

/**
 * @author vilaca
 * 
 */
public class GzipResponse extends AbstractResponse {

	final byte[] body;
	final byte[] zipBody;
	final String mime;
	private boolean zipped;

	/**
	 * c'tor
	 * 
	 * @param body
	 * @param mime
	 */
	public GzipResponse(byte[] body, String mime) {

		this.body = body;
		this.zipBody = zipBody(body);
		this.mime = mime;
	}

	private byte[] zipBody(byte[] body) {

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				GZIPOutputStream zip = new GZIPOutputStream(baos);) {

			zip.write(body);
			zip.flush();
			zip.close();

			return baos.toByteArray();

		} catch (IOException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pt.go2.application.HttpResponse#getHttpErrorCode()
	 */
	@Override
	public int getHttpStatus() {
		return 200;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pt.go2.application.HttpResponse#getMimeType()
	 */
	@Override
	public String getMimeType() {
		return mime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pt.go2.application.HttpResponse#getBody(com.sun.net.httpserver.HttpExchange
	 * )
	 */
	@Override
	public byte[] run(HttpExchange exchange) {

		if (this.zipBody != null && clientAcceptsZip(exchange)) {
			zipped = true;
			return zipBody;
		}

		zipped = false;
		return body;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pt.go2.application.HttpResponse#isZipped()
	 */
	@Override
	public boolean isZipped() {
		return zipped;
	}

	/**
	 * Does the client accept a ziped response?
	 * 
	 * @param exchange
	 * 
	 * @return
	 */
	private boolean clientAcceptsZip(final HttpExchange exchange) {

		final Headers headers;
		final List<String> values;

		headers = exchange.getRequestHeaders();
		values = headers.get(REQUEST_HEADER_ACCEPT_ENCODING);

		return values.size() > 0 && values.get(0).indexOf("gzip") != -1;
	}
}
