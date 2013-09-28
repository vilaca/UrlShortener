/**
 * 
 */
package pt.go2.services;

/**
 * @author vilaca
 * 
 */
public class HttpResponse {

	private final String mimeType;
	private final byte[] body;
	private final int httpError;
	private final boolean isZipped;

	public static HttpResponse createBadRequest() {
		// TODO return static instead of creating new one ?
		return new HttpResponse(null, null, 400, false);
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

	public boolean success() {
		return httpError == 200 || httpError == 301 || httpError == 302;
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
