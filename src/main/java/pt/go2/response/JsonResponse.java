package pt.go2.response;

import javax.servlet.http.HttpServletResponse;

public class JsonResponse extends AbstractResponse {

	final byte[] body;
	
	public JsonResponse ( final String body )
	{
		this.body = body.getBytes();
	}
	
	@Override
	public int getHttpStatus() {
		return 200;
	}

	@Override
	public byte[] run(HttpServletResponse exchange) {
		return body;
	}
	
	@Override
	public String getMimeType() {
		return MIME_APP_JSON;
	}

}
