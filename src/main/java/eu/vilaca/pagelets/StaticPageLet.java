package eu.vilaca.pagelets;

import com.sun.net.httpserver.HttpExchange;

public class StaticPageLet extends PageLet {

	final byte[] content;
	final private int responseCode;
	final private String mimeType;

	protected StaticPageLet(final byte[] content, final String mimeType,
			final int responseCode) {
		this.content = content;
		this.responseCode = responseCode;
		this.mimeType = mimeType;
	}

	public StaticPageLet(final byte[] content, final String mimeType) {
		this(content, mimeType, 200);
	}

	public StaticPageLet(final byte[] content) {
		this(content, "text/html");
	}

	public StaticPageLet(final byte[] content, final int responseCode) {
		this(content, "text/html", responseCode);
	}

	@Override
	byte[] main(final HttpExchange exchange) {
		return content;
	}

	@Override
	public int getResponseCode() {
		return responseCode;
	}

	@Override
	public String getMimeType() {
		return mimeType;
	}
}
