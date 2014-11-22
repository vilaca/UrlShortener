package pt.go2.fileio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Loads domain WhiteList
 */
public class WhiteList {

	private static final String FILENAME = "whitelist";

	private static final Logger LOGGER = LogManager.getLogger();

	final Set<String> whitelist = new HashSet<String>();

	/**
	 * Create WhiteList
	 */
	public static WhiteList create() {

		try (InputStream is = Configuration.class.getResourceAsStream("/" + FILENAME);) {

			final InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			final BufferedReader br = new BufferedReader(isr);
			final WhiteList wl = new WhiteList();

			String line;

			while ((line = br.readLine()) != null) {

				if (line.isEmpty() || line.startsWith("#"))

					continue;

				LOGGER.info("Adding " + line + " to whitelist.");

				wl.whitelist.add(line);

				// next line
			}

			return wl;

		} catch (IOException e2) {

			LOGGER.error("Can't read from whitelist file.");
		}

		return null;
	}

	public boolean contains(String url) {
		return whitelist.contains(url);
	}
}
