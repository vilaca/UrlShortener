package pt.go2.fileio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse files w/ smart tags
 */
public final class SmartTagParser {

	private final String base;
	
	private final Pattern tagPattern = Pattern.compile( "\\[\\$\\w*\\$\\]");
	private final Pattern tagFuncPattern = Pattern.compile("\\[\\$\\w*(.*)\\$\\]");
	private final Pattern tagFuncNamePattern = Pattern.compile("^\\w*");
	private final Pattern tagFuncParamPattern = Pattern.compile("\\(.*\\)");

	public SmartTagParser(final String base) {
		this.base = base;
	}

	public byte[] read(final String filename) throws IOException {
		try (final BufferedReader br = new BufferedReader(
				new InputStreamReader(
						SmartTagParser.class
								.getResourceAsStream(base + filename)));) {

			return readFromFile(br);
		}
	}

	private byte[] readFromFile(final BufferedReader br) throws IOException {

		final StringBuilder sb = new StringBuilder();

		String line = br.readLine();

		while (line != null) {

			if (line.isEmpty() || line.charAt(0) == '#')
			{
				line = br.readLine();
				continue;
			}
			
			final Matcher tagMatcher = tagPattern.matcher(line);

			while (tagMatcher.find()) {
				
				String tag = tagMatcher.group();
				tag = tag.substring(2, tag.length() - 2);
				
				final String value = Configuration.getProperty("web." + tag);
				
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
