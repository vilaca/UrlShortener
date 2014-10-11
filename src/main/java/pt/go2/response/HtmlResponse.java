package pt.go2.response;

import javax.servlet.http.HttpServletResponse;

/**
 * Used when the client asks to hash an Url
 */
public class HtmlResponse extends AbstractResponse {

	final byte[] body;

	public HtmlResponse(final byte[] body) {
		this.body = body;
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
		// TODO hash return is not really HTML
		return MIME_TEXT_HTML;
	}
}
