/**
 * 
 */
package eu.vilaca.keystore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author vilaca
 * 
 */
public class Database {

	static final Logger logger = LogManager.getLogger(Database.class.getName());

	// TODO make this a singleton to avoid statics?

	// hash map to URL
	// TODO can be optimized, only one MAP is needed
	final static Map<HashKey, String> hash2Url = new HashMap<HashKey, String>();
	final static Map<String, HashKey> url2Hash = new HashMap<String, HashKey>();

	static BufferedWriter resumeLog;

	/**
	 * private c'tor to forbid instantiation
	 */
	private Database() {
	}

	public static void start(String folder) throws IOException {

		// fix database path

		if (folder == null) {

			// default to base directory
			folder = "";

		} else if (!folder.endsWith(System.getProperty("file.separator"))) {
			folder += System.getProperty("file.separator");
		}

		logger.trace("Resuming from folder: " + folder);

		// get all files sorted

		final File[] files = new File(folder).listFiles();
		final int next;

		if (files != null && files.length > 0) {

			logger.trace("Found " + files.length + " old files.");

			// read old data

			readSerializedData(files);

			// get most recent file ( last in array )

			Arrays.sort(files);

			final File latest = files[files.length - 1];

			next = Integer.parseInt(latest.getName()) + 1;

		} else {
			next = 0;
		}

		final String filename = folder + String.format("%05d", next);

		logger.trace("Next (resume) file: " + filename);

		resumeLog = new BufferedWriter(new FileWriter(filename));
	}

	public static void stop() throws IOException {
		resumeLog.flush();
		resumeLog.close();
	}

	/**
	 * @param files
	 */
	private static void readSerializedData(final File[] files) {

		// load all data from each file

		for (final File file : files) {
			try (final FileReader fr = new FileReader(file.getAbsolutePath());
					final BufferedReader br = new BufferedReader(fr);) {

				logger.trace("Reading from Resume file: " + file.getName());

				// read all lines in file

				String line = br.readLine();

				while (line != null) {

					// fields are comma separated [hashkey,url]

					final String fields[] = line.split(",");
					final HashKey hk = new HashKey(fields[0].getBytes());
					final String url = fields[1];

					// store data

					hash2Url.put(hk, url);
					url2Hash.put(url, hk);

					line = br.readLine();
				}

			} catch (IOException e) {
				logger.error("Error reading: " + file.getAbsolutePath());
			}
		}
	}

	public static byte[] add(String url) {

		// trim whitespace

		url = url.trim();

		// only continue if its a valid Url

		if (!new UrlValidator(new String[] { "http", "https" }).isValid(url)) {
			return null;
		}

		// normalize Url ending

		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}

		// remove http:// but keep https://

		if (url.startsWith("http://")) {
			url = url.substring("http://".length());
		}

		// lookup database to see if URL is already there

		HashKey base64hash = url2Hash.get(url);

		if (base64hash != null) {
			return base64hash.getBytes();
		}

		// TODO check granularity, may add a random value to avoid too many
		// repeats in case of hash collision
		int tries = 0;
		HashKey hk;
		do {

			tries++;
			if (tries > 10) {
				// give up
				return null;
			}

			// create a base64 hash based on system timer
			final long ticks = hash2Url.hashCode() ^ System.currentTimeMillis()
					* url2Hash.hashCode();
			hk = new HashKey(ticks * tries);

			// loop if hash already being used

		} while (hash2Url.containsKey(hk));

		hash2Url.put(hk, url);
		url2Hash.put(url, hk);

		try {
			resumeLog.write(hk.toString() + "," + url
					+ System.getProperty("line.separator"));
			resumeLog.flush();

		} catch (IOException e) {

			hash2Url.remove(hk);
			url2Hash.remove(url);

			return null;
		}

		return hk.getBytes();
	}

	public static String get(final HashKey key) {

		final String url = hash2Url.get(key);

		if (url == null) {
			return null;
		}

		if (url.startsWith("http")) {
			return url;
		}

		return "http://" + url;
	}

}
