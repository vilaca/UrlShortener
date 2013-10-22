package pt.go2.application;

/**
 * @author vilaca
 * 
 */
public class HttpResponse {

	private final static HttpResponse BAD_REQUEST = new HttpResponse("text/plain", "Bad request.".getBytes(), 400, false);
	
	private final String mimeType;
	private final byte[] body;
	private final int httpError;
	private final boolean isZipped;

	public static HttpResponse createBadRequest() {
		return BAD_REQUEST;
	}

	public static HttpResponse createZipped(String mimeType, byte[] body,
			int httpError) {
		return new HttpResponse(mimeType, body, httpError, true);
	}

	public static HttpResponse create(String mimeType, byte[] body,
			int httpError) {
		return new HttpResponse(mimeType, body, httpError, false);
	}

	private HttpResponse(String mimeType, byte[] body, int httpError,
			boolean zipped) {
		this.httpError = httpError;
		this.mimeType = mimeType;
		this.body = body;
		this.isZipped = zipped;
	}

	public int getHttpErrorCode() {
		return httpError;
	}

	public String getMimeType() {
		return mimeType;
	}

	public int getSize() {
		return body.length;
	}

	public byte[] getBody() {
		return body;
	}

	public boolean isZipped() {
		return isZipped;
	}

}
