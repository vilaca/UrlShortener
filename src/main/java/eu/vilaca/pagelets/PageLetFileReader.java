/**
 * 
 */
package eu.vilaca.pagelets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author vilaca
 * 
 */
public class PageLetFileReader {

	private final Properties properties;
	private final String base;

	public PageLetFileReader(final String base, final Properties properties) {
		this.base = base;
		this.properties = properties;
	}

	public byte[] read(final String filename) throws IOException
	{
		try (final InputStream input = StaticPageLet.class
				.getResourceAsStream(base + filename);
				final ByteArrayOutputStream baos = new ByteArrayOutputStream();) {

			if (input == null) {
				throw new IOException("Could not open: " + filename + "."); //
			}

			readFromFile(input, baos);
			baos.flush();

			return baos.toByteArray();
		}
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

}
