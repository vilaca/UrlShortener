package pt.go2.response;

import com.sun.net.httpserver.HttpExchange;

public class JsonResponse extends AbstractResponse {

	final byte[] body;

	public JsonResponse(final String body) {
		this(body.getBytes());
	}

	public JsonResponse(final byte[] body) {
		this.body = body;
	}

	@Override
	public int getHttpStatus() {
		return 200;
	}

	@Override
	public byte[] run(final HttpExchange exchange) {
		return body;
	}

	@Override
	public String getMimeType() {
		return MIME_APP_JSON;
	}

}
