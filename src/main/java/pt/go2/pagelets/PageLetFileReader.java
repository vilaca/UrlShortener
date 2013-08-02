/**
 * 
 */
package pt.go2.pagelets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.go2.services.PropertiesManager;


/**
 * @author vilaca
 * 
 */
public final class PageLetFileReader {

	private static final String SMART_TAG_MASK = "\\[\\$\\w*\\$\\]";
	private final Properties properties = PropertiesManager.getProperties();
	private final String base;
	private final Pattern pattern = Pattern.compile(SMART_TAG_MASK);

	public PageLetFileReader(final String base) {
		this.base = base;
	}

	public byte[] read(final String filename) throws IOException {
		try (final BufferedReader br = new BufferedReader(
				new InputStreamReader(
						PageLetFileReader.class
								.getResourceAsStream(base + filename)));) {

			return readFromFile(br);
		}
	}

	/**
	 * @param input
	 * @param baos
	 * @throws IOException
	 */
	private byte[] readFromFile(final BufferedReader br) throws IOException {

		final StringBuilder sb = new StringBuilder();

		String line = br.readLine();

		while (line != null) {
			Matcher matcher = pattern.matcher(line);

			while (matcher.find()) {
				
				String tag = matcher.group();
				tag = tag.substring(2, tag.length() - 2);
				
				final String value = properties.getProperty("web." + tag);
				
				if ( value != null) line = matcher.replaceAll(value);
			}

			// don't use "system/OS" definition here, \r\n is what is needed
			sb.append(line + "\r\n");
			
			line = br.readLine();
		}

		return sb.toString().getBytes();
	}
}
