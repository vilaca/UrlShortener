package pt.go2.application;

import com.sun.net.httpserver.HttpExchange;

/**
 * Abstract class for server response
 */
abstract class AbstractResponse {

	// Request headers
	public static final String REQUEST_HEADER_ACCEPT_ENCODING = "Accept-encoding";
	public static final String REQUEST_HEADER_HOST = "Host";
	
	// Response headers
	public static final String RESPONSE_HEADER_CACHE_CONTROL = "Cache-Control";
	public static final String RESPONSE_HEADER_CONTENT_ENCODING = "Content-Encoding";
	public static final String RESPONSE_HEADER_CONTENT_TYPE = "Content-Type";
	public static final String RESPONSE_HEADER_LOCATION = "Location";
	public static final String RESPONSE_HEADER_SERVER = "Server";

	// mime types
	public static final String MIME_APP_JAVASCRIPT = "application/javascript";
	public static final String MIME_TEXT_CSS = "text/css";
	public static final String MIME_TEXT_HTML = "text/html";
	public static final String MIME_TEXT_PLAIN = "text/plain";
	public static final String MIME_TEXT_XML = "text/xml";

	/**
	 * Http Status code for response
	 * 
	 * @return
	 */
	abstract public int getHttpStatus();

	/**
	 * Generate response
	 * 
	 * @param exchange
	 * @return
	 */
	abstract byte[] run(final HttpExchange exchange);

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
