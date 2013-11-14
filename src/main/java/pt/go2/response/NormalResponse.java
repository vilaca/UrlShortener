package pt.go2.response;

import com.sun.net.httpserver.HttpExchange;

/**
 * Used when the client asks to hash an Url
 */
public class NormalResponse extends AbstractResponse {

	final byte[] body;

	public NormalResponse(final byte[] body) {
		this.body = body;
	}

	@Override
	public int getHttpStatus() {
		return 200;
	}

	@Override
	public byte[] run(HttpExchange exchange) {
		return body;
	}
	
	@Override
	public String getMimeType() {
		// TODO hash return is not really HTML
		return MIME_TEXT_HTML;
	}
}
