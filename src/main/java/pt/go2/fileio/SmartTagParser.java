package pt.go2.fileio;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse files w/ smart tags
 */
public final class SmartTagParser {

	private static final Pattern tagPattern = Pattern.compile("\\[\\$\\w*\\$\\]");

	public static byte[] read(final InputStream file, Configuration conf) throws IOException {

		try (final BufferedReader br = new BufferedReader(new InputStreamReader(file));) {

			return readFromFile(br, conf);
		}
	}

	private static byte[] readFromFile(final BufferedReader br, Configuration conf) throws IOException {

		final StringBuilder sb = new StringBuilder();

		String line = br.readLine();

		while (line != null) {

			if (line.trim().isEmpty() || line.charAt(0) == '#') {
				line = br.readLine();
				continue;
			}

			final Matcher tagMatcher = tagPattern.matcher(line);

			while (tagMatcher.find()) {

				String tag = tagMatcher.group();
				tag = tag.substring(2, tag.length() - 2);

				final String value;

				if (tag.equals("date")) {

					value = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

				} else {

					value = conf.getProperty("web." + tag);
				}

				if (value != null) {
					line = tagMatcher.replaceFirst(value);
				}
			}

			// don't use "system/OS" definition here, \r\n is HTTP specified
			// newline
			sb.append(line + "\r\n");

			line = br.readLine();
		}

		return sb.toString().getBytes();
	}

	public static byte[] read(String filename, Configuration conf) throws FileNotFoundException, IOException {

		try (final InputStream br = new FileInputStream(filename);) {
			return read(br, conf);
		}
	}
}
