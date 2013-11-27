package pt.go2.response;

import com.sun.net.httpserver.HttpExchange;

/**
 * Simple response - Any status, mime and content
 */
public class SimpleResponse extends AbstractResponse {

	final byte[] body;
	final int status;
	final String mime;

	/**
	 * c'tor
	 * 
	 * @param body
	 * @param status
	 * @param mime
	 */
	public SimpleResponse(byte[] body, int status, String mime) {

		this.body = body;
		this.status = status;
		this.mime = mime;
	}

	/**
	 * C'tor for responses with an empty body
	 * 
	 * @param status
	 * @param mime
	 */
	SimpleResponse(int status, String mime) {

		this(new byte[] {}, status, mime);
	}

	@Override
	public int getHttpStatus() {
		return status;
	}

	@Override
	public String getMimeType() {
		return mime;
	}

	@Override
	public byte[] run(HttpExchange exchange) {
		return body;
	}
}
