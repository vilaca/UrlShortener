package pt.go2.fileio;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

	private static final Pattern PATTERN = Pattern.compile("\\[\\$\\w*\\$\\]");
	private static final Pattern FUNCPATTERN = Pattern.compile("\\[\\$\\w*(.*)\\$\\]");
	private static final Pattern TAGFUNCNAMEPATTERN = Pattern.compile("^\\w*");
	private static final Pattern TAGFUNCPARAMPATTERN = Pattern.compile("\\(.*\\)");

	/**
	 * private c'tor to forbid instantiation
	 */
	private SmartTagParser() {
	}

	public static byte[] read(final InputStream file) throws IOException {
		try (final BufferedReader br = new BufferedReader(
				new InputStreamReader(file));) {

			return readFromFile(br);
		}
	}

	public static byte[] read(String filename) throws FileNotFoundException,
			IOException {

		try (final InputStream br = new FileInputStream(filename);) {
			return read(br);
		}
	}

	private static byte[] readFromFile(final BufferedReader br)
			throws IOException {

		final StringBuilder sb = new StringBuilder();

		String line = br.readLine();

		while (line != null) {

			if (line.isEmpty() || line.charAt(0) == '#') {
				line = br.readLine();
				continue;
			}

			final Matcher tagMatcher = PATTERN.matcher(line);

			while (tagMatcher.find()) {

				String tag = tagMatcher.group();
				tag = tag.substring(2, tag.length() - 2);

				final String value = Configuration.getProperty("web." + tag);

				if (value != null)
					line = tagMatcher.replaceFirst(value);
			}

			final Matcher tagFuncMatcher = FUNCPATTERN.matcher(line);

			while (tagFuncMatcher.find()) {

				String tag = tagFuncMatcher.group();
				tag = tag.substring(2, tag.length() - 2);

				final Matcher tagFuncNameMatcher = TAGFUNCNAMEPATTERN
						.matcher(tag);

				if (!tagFuncNameMatcher.find())
					break; // wrong positive ?

				final String name = tagFuncNameMatcher.group();

				// add more keywords later?
				if (name.equals("date")) {
					final Matcher tagFuncParamMatcher = TAGFUNCPARAMPATTERN
							.matcher(tag);

					if (!tagFuncParamMatcher.find())
						break; // wrong positive ?

					String param = tagFuncParamMatcher.group();
					param = param.substring(1, param.length() - 1);

					final Date now = Calendar.getInstance().getTime();

					final String date = new SimpleDateFormat(param).format(now);

					tagFuncMatcher.replaceFirst(date);
				}
			}

			// don't use "system/OS" definition here, \r\n is HTTP specified
			// newline
			sb.append(line + "\r\n");

			line = br.readLine();
		}

		return sb.toString().getBytes();
	}
}
