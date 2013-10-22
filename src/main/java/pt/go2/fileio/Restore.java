package pt.go2.fileio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Restore {

	private static final Logger logger = LogManager.getLogger(Restore.class);

	/**
	 * Class to hold data records from restore files
	 */
	public static class RestoreItem {

		public final String Key;
		public final String Value;

		public RestoreItem(final String Key, final String Value) {
			this.Key = Key;
			this.Value = Value;
		}
	}

	/**
	 * Load hashes from disk and turn on logging
	 * 
	 * @param folder
	 * @throws IOException
	 */
	public static List<RestoreItem> start(String folder) {

		final File[] files = new File(folder).listFiles();

		if (files == null || files.length == 0) {
			return null;
		}

		logger.trace("Found " + files.length + " restore files.");

		List<RestoreItem> items = new ArrayList<RestoreItem>();

		for (final File file : files) {

			// load all data from each file

			try (final FileReader fr = new FileReader(file.getAbsolutePath());
					final BufferedReader br = new BufferedReader(fr);) {

				logger.trace("Reading from Resume file: " + file.getName());

				// read all lines in file

				String line = br.readLine();

				while (line != null) {

					// fields are comma separated [ hash key, URI ]

					final String hashkey = line.substring(0, 6);
					final String uri = line.substring(7);

					items.add(new RestoreItem(hashkey, uri));

					// next line

					line = br.readLine();
				}

			} catch (IOException e) {

				logger.error("Error reading: " + file.getAbsolutePath());
				
			}
		}
		
		return items;
	}
}
