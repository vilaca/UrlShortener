package pt.go2.response;

import com.sun.net.httpserver.HttpExchange;

/**
 * Error response - Http status 400, 404, etc
 */
public class ErrorResponse extends AbstractResponse {

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
	public ErrorResponse(byte[] body, int status, String mime) {

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
	ErrorResponse(int status, String mime) {

		this("".getBytes(), status, mime);
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
