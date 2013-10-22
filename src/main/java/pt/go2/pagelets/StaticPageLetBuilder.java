package pt.go2.pagelets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * Builder object to for immutable StaticPageLet
 * 
 */
public class StaticPageLetBuilder {

	private byte[] content;
	private byte[] zipped;
	private int responseCode = 200; // HTTP OK
	private String mimeType = "text/html";

	public StaticPageLet build() {
		return new StaticPageLet(content, mimeType, responseCode, zipped);
	}

	/**
	 * Zip response contents
	 * 
	 * @return
	 * @throws IOException
	 */
	public StaticPageLetBuilder zip() throws IOException {

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				GZIPOutputStream zip = new GZIPOutputStream(baos);) {

			zip.write(content);
			zip.close(); // has to be closed before baos can be read
			this.zipped = baos.toByteArray();
		}
		return this;
	}

	/**
	 * Response contents
	 * 
	 * @param content
	 * @return
	 */
	public StaticPageLetBuilder setContent(final byte[] content) {
		this.content = content;
		return this;
	}

	/**
	 * Set response code, usually 200 for OK
	 * 
	 * @param responseCode
	 * @return
	 */
	public StaticPageLetBuilder setResponseCode(final int responseCode) {
		this.responseCode = responseCode;
		return this;
	}

	/**
	 * Set MIME type
	 * 
	 * @param mimeType
	 * @return
	 */
	public StaticPageLetBuilder setMimeType(final String mimeType) {
		this.mimeType = mimeType;
		return this;
	}
}
