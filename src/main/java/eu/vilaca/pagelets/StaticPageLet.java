package eu.vilaca.pagelets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

	public static PageLet fromFile(final String filename,
			final String mimeType, final int responseCode) throws IOException {

		// TODO: remove this block to its own class?

		try (final InputStream input = StaticPageLet.class
				.getResourceAsStream(filename);
				final ByteArrayOutputStream baos = new ByteArrayOutputStream();) {

			if (input == null) {
				throw new IOException("Could not open: " + filename + "."); //
			}

			readFromFile(input, baos);
			baos.flush();

			return new StaticPageLet(baos.toByteArray(), mimeType, responseCode);
		}
	}

	public static PageLet fromFile(final String filename) throws IOException {
		return fromFile(filename, "text/html", 200);
	}

	public static PageLet fromFile(final String filename, final String mimeType)
			throws IOException {
		return fromFile(filename, mimeType, 200);
	}

	public static PageLet fromFile(final String filename, final int responseCode)
			throws IOException {
		return fromFile(filename, "text/html", responseCode);
	}

	/**
	 * @param input
	 * @param baos
	 * @throws IOException
	 */
	private static void readFromFile(final InputStream input,
			final ByteArrayOutputStream baos) throws IOException {

		final byte[] buffer = new byte[4096];

		do {
			final int read = input.read(buffer);

			if (read == -1)
				break;

			baos.write(buffer, 0, read);

		} while (true);
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
