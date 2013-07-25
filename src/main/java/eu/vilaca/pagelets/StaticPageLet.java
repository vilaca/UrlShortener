package eu.vilaca.pagelets;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

/*
 * Immutable class for static pages, including  
 */
public class StaticPageLet extends AbstractPageLet {

	final private byte[] content;
	final private int responseCode;
	final private String mimeType;

	/**
	 * Builder object to build immutable StaticPageLet
	 * 
	 * @author vilaca
	 *
	 */
	static public class Builder
	{
		private byte[] content;
		private int responseCode = 200; // HTTP OK
		private String mimeType = "text/html";
		
		public StaticPageLet build ()
		{
			return new StaticPageLet( content, mimeType, responseCode);
		}

		public Builder setContent(final byte[] content) {
			this.content = content;
			return this;
		}

		public Builder setResponseCode(final int responseCode) {
			this.responseCode = responseCode;
			return this;
		}

		public Builder setMimeType(final String mimeType) {
			this.mimeType = mimeType;
			return this;
		}
	}
	
	private StaticPageLet(final byte[] content, final String mimeType,
			final int responseCode) {
		this.content = content;
		this.responseCode = responseCode;
		this.mimeType = mimeType;
	}

	@Override
	public int getResponseCode() {
		return responseCode;
	}

	@Override
	public String getMimeType() {
		return mimeType;
	}

	/*
	 * Only return static content here
	 * 
	 * (non-Javadoc)
	 * @see eu.vilaca.pagelets.AbstractPageLet#main(com.sun.net.httpserver.HttpExchange)
	 */
	@Override
	byte[] main(HttpExchange exchange) throws IOException {
		return this.content;
	}
}
