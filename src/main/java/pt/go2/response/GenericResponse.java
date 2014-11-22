package pt.go2.response;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

public class GenericResponse extends AbstractResponse {

	final byte[] body;
	final int error;
	final String mime;
	
	public GenericResponse(byte[] body, int error, String mime) {
		this.body = body;
		this.error = error;
		this.mime = mime;
	}

	public GenericResponse(int error) {
		this.body = new byte[0];
		this.error = error;
		this.mime = MIME_TEXT_PLAIN;
	}

	public GenericResponse(byte[] body) {
		this.body = body;
		this.error = HttpStatus.OK_200;
		this.mime = MIME_TEXT_PLAIN;
	}

	@Override
	public int getHttpStatus() {
		return error;
	}

	@Override
	public byte[] run(HttpServletResponse exchange) {
		return body;
	}
	
	@Override
	public String getMimeType() {
		return mime;
	}
}
