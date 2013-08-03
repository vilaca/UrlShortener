/**
 * 
 */
package pt.go2.pagelets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.go2.services.PropertiesManager;


/**
 *
 * @author vilaca
 * 
 */
public final class PageLetFileReader {

	private final Properties properties = PropertiesManager.getProperties();
	private final String base;
	
	private final Pattern tagPattern = Pattern.compile( "\\[\\$\\w*\\$\\]");
	private final Pattern tagFuncPattern = Pattern.compile("\\[\\$\\w*(.*)\\$\\]");
	private final Pattern tagFuncNamePattern = Pattern.compile("^\\w*");
	private final Pattern tagFuncParamPattern = Pattern.compile("\\(.*\\)");

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
	 * 
	 * 
	 * @param input
	 * @param baos
	 * 
	 * @throws IOException
	 */
	private byte[] readFromFile(final BufferedReader br) throws IOException {

		final StringBuilder sb = new StringBuilder();

		String line = br.readLine();

		while (line != null) {

			if ( line.charAt(0) == '#' ) continue;
			
			final Matcher tagMatcher = tagPattern.matcher(line);

			while (tagMatcher.find()) {
				
				String tag = tagMatcher.group();
				tag = tag.substring(2, tag.length() - 2);
				
				final String value = properties.getProperty("web." + tag);
				
				if ( value != null) line = tagMatcher.replaceFirst(value);
			}

			final Matcher tagFuncMatcher = tagFuncPattern.matcher(line);

			while (tagFuncMatcher.find()) {
				
				String tag = tagFuncMatcher.group();
				tag = tag.substring(2, tag.length() - 2);
				
				final Matcher tagFuncNameMatcher = tagFuncNamePattern.matcher(tag);
				
				if (!tagFuncNameMatcher.find()) break; // wrong positive ?
				
				final String name = tagFuncNameMatcher.group();

				// add more keywords later?
				if (name.equals("date")) {
					final Matcher tagFuncParamMatcher = 
							tagFuncParamPattern.matcher(tag);

					if (!tagFuncParamMatcher.find()) break; // wrong positive ?
					
					String param = tagFuncParamMatcher.group();
					param = param.substring(1, param.length() - 1);
					
					final Date now = Calendar.getInstance().getTime();

					final String date = new SimpleDateFormat(param).format(now);

					tagFuncMatcher.replaceFirst(date);
				}
			}

			// don't use "system/OS" definition here, \r\n is HTTP specified newline
			sb.append(line + "\r\n");
			
			line = br.readLine();
		}

		return sb.toString().getBytes();
	}
}
