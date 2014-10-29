package pt.go2.response;

import javax.servlet.http.HttpServletResponse;

/**
 * Informs the client the URL hash validation is still being processed
 *
 */
public class ProcessingResponse extends AbstractResponse {

	@Override
	public int getHttpStatus() {
		return 202;
	}

	@Override
	public byte[] run(HttpServletResponse exchange) {
		return "".getBytes();
	}
}
