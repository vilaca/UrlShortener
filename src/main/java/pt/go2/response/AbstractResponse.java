package pt.go2.response;

import javax.servlet.http.HttpServletResponse;

/**
 * Abstract class for server response
 */
public abstract class AbstractResponse {

	// Request headers
	public static final String REQUEST_HEADER_ACCEPT_ENCODING = "Accept-encoding";
	public static final String REQUEST_HEADER_HOST = "Host";
	public static final String REQUEST_HEADER_REFERER = "Referer";
	public static final String REQUEST_HEADER_USER_AGENT = "User-Agent";

	// Response headers
	public static final String RESPONSE_HEADER_CACHE_CONTROL = "Cache-Control";
	public static final String RESPONSE_HEADER_CONTENT_ENCODING = "Content-Encoding";
	public static final String RESPONSE_HEADER_CONTENT_TYPE = "Content-Type";
	public static final String RESPONSE_HEADER_EXPIRES = "Expires";
	public static final String RESPONSE_HEADER_LOCATION = "Location";
	public static final String RESPONSE_HEADER_SERVER = "Server";

	// mime types
	public static final String MIME_APP_JAVASCRIPT = "application/javascript";
	public static final String MIME_APP_JSON = "application/json";

	public static final String MIME_IMG_JPEG = "image/jpeg";
	public static final String MIME_IMG_GIF = "image/gif";
	public static final String MIME_IMG_PNG = "image/png";
	
	public static final String MIME_TEXT_CSS = "text/css";
	public static final String MIME_TEXT_HTML = "text/html";
	public static final String MIME_TEXT_PLAIN = "text/plain";
	public static final String MIME_TEXT_XML = "text/xml";

	/**
	 * Http Status code for response
	 * 
	 * @return
	 */
	public abstract int getHttpStatus();

	/**
	 * Generate response
	 * 
	 * @param exchange
	 * @return
	 */
	public abstract byte[] run(final HttpServletResponse exchange);

	/**
	 * Implementations should override mime type when necessary
	 * 
	 * @return
	 */
	public String getMimeType() {
		return MIME_TEXT_PLAIN;
	}

	/**
	 * Implementations that return gzip content must override
	 * 
	 * @return
	 */
	public boolean isZipped() {
		return false;
	}
}