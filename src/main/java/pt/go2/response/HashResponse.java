package pt.go2.response;

import com.sun.net.httpserver.HttpExchange;

/**
 * Used when the client asks to hash an Url
 */
public class HashResponse extends AbstractResponse {

	final byte[] body;

	public HashResponse(final byte[] body) {
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
	public boolean isCacheable() {
		return false;
	}

}
