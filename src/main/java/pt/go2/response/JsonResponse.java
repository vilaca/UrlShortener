package pt.go2.response;

import com.sun.net.httpserver.HttpExchange;

public class JsonResponse extends AbstractResponse {

	final byte[] body;
	
	JsonResponse ( final String body )
	{
		this.body = body.getBytes();
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
		return MIME_APP_JSON;
	}

}
