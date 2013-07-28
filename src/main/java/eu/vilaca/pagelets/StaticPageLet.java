package eu.vilaca.pagelets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

/*
 * Immutable class for static pages, including  
 */
public class StaticPageLet extends AbstractPageLet {

	final private byte[] content;
	final private byte[] gzipped;
	final private int responseCode;
	final private String mimeType;
	final private String redirect;

	/**
	 * Builder object to build immutable StaticPageLet
	 * 
	 * @author vilaca
	 * 
	 */
	static public class Builder {
		private byte[] content;
		private byte[] zipped;
		private int responseCode = 200; // HTTP OK
		private String mimeType = "text/html";
		private String redirect = null;

		public StaticPageLet build() {
			return new StaticPageLet(content, mimeType, responseCode, redirect,
					zipped);
		}

		public Builder zip() throws IOException {
			
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
					GZIPOutputStream zip = new GZIPOutputStream(baos);) {

				zip.write(content);
				zip.close(); // has to be closed before baos can be read
				this.zipped = baos.toByteArray();
			}
			return this;
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

		public Builder setRedirect(String url) {
			this.redirect = url;
			return this;
		}
	}

	private StaticPageLet(final byte[] content, final String mimeType,
			final int responseCode, final String redirect, final byte[] gzipped) {
		this.content = content;
		this.responseCode = responseCode;
		this.mimeType = mimeType;
		this.redirect = redirect;
		this.gzipped = gzipped;
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
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.vilaca.pagelets.AbstractPageLet#main(com.sun.net.httpserver.HttpExchange
	 * )
	 */
	@Override
	byte[] main(final HttpExchange exchange) throws IOException {

		if (redirect != null) {
			exchange.getResponseHeaders().set("Location", redirect);
			return this.content;	
		}

		if (this.gzipped == null)
		{
			return this.content;
		}
		
		Headers headers = exchange.getRequestHeaders();
		List<String> values = headers.get("Accept-encoding");
		boolean sendZipped =  values.size() > 0 && values.get(0).indexOf("gzip") != -1; 
		
		if ( sendZipped )
		{
			exchange.getResponseHeaders().set("Content-Encoding", "gzip");
			return this.gzipped;
		}
		
		return this.content;
			
	}
}
