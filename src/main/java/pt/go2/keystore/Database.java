package pt.go2.keystore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.pagelets.StaticPageLet;
import pt.go2.services.PropertiesManager;


/**
 * Database implemented as Singleton
 * 
 * Holds all mappings on URLs to HASHED keys and vice versa
 * 
 * @author vilaca
 * 
 */
public class Database {

	static final Logger logger = LogManager.getLogger(Database.class.getName());

	// for singleton
	static final private Database inner = new Database();

	// hash to URL
	final private Map<HashKey, StaticPageLet> hash2Url = new HashMap<HashKey, StaticPageLet>(1600000);
	
	// URL to hash
	final private Map<AsciiString, HashKey> url2Hash = new HashMap<AsciiString, HashKey>(1600000);

	// used to read configuration
	final private Properties properties = PropertiesManager.getProperties();
	
	// configurable redirect code
	private int redirectCode;
	
	// log hashes to disk
	private BufferedWriter resumeLog;

	/**
	 * private c'tor to forbid instantiation
	 */
	private Database() {
	}

	/**
	 * Get Database instance
	 * 
	 * @return
	 */
	static public Database getDatabase() {
		return inner;
	}

	/**
	 * Start database: Load hashes from disk and turn on logging
	 * 
	 * @param folder
	 * @throws IOException
	 */
	public void start(String folder) throws IOException {

		// fix database path

		if (folder == null) {

			// default to base directory
			folder = "";

		} else if (!folder.endsWith(System.getProperty("file.separator"))) {
			folder += System.getProperty("file.separator");
		}
		
		logger.trace("Current relative path is:" + Paths.get("").toAbsolutePath().toString());		
		logger.trace("Resuming DB from folder: " + folder);

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

		final String filename = folder + String.format("%07d", next);

		logger.info("Next (resume) file: " + filename);

		resumeLog = new BufferedWriter(new FileWriter(filename));
	}

	/**
	 * Read hashes persistent in disk
	 * 
	 * @param files
	 */
	private void readSerializedData(final File[] files) {

		redirectCode = getRedirectCode();
		
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
					
					if ( fields.length < 2 )
					{
						logger.error("Error on DB restore file. Skipping.");
						continue;
					}
					
					final HashKey hk = new HashKey(fields[0].getBytes());
					final String url = fields[1];

					// store data

					storeHash(hk, url);

					line = br.readLine();
				}

			} catch (IOException e) {
				logger.error("Error reading: " + file.getAbsolutePath());
			}
		}
	}

	private void storeHash(final HashKey hk, final String url) {
		final String completeUrl = url.startsWith("http") ? url : "http://" + url;
		
		logger.trace("Stored URI (" + url + ") is now (" + completeUrl + ").");
		
		StaticPageLet redirect = 
				new StaticPageLet.Builder()
				.setContent(new byte[0])
				.setResponseCode(redirectCode)
				.setRedirect(completeUrl).build();
		
		hash2Url.put(hk, redirect);
		url2Hash.put(new AsciiString(url), hk);
	}

	/**
	 * Read http response code used for redirection. Usually 301 or 302(default).
	 * 
	 * @return
	 */
	private int getRedirectCode() {
		final String _redirectCode = properties.getProperty("server.redirect");
		
		int redirectCode;
		if ( _redirectCode != null)
		{
			try
			{
				redirectCode = Integer.parseInt(_redirectCode);
			}
			catch(NumberFormatException ex)
			{
				redirectCode = 302;
			}
		}
		else
		{
			redirectCode = 302;
		}
		return redirectCode;
	}

	/**
	 * Add Url to Database
	 * 
	 * @param url
	 * @return
	 */
	public byte[] add(String url) {

		// trim whitespace

		url = url.trim();

		// only continue if its a valid Url

		if (!new UrlValidator(new String[] { "http", "https" }).isValid(url)) {
			return null;
		}

		url = normalizeUrl(url);

		// lookup database to see if URL is already there

		HashKey base64hash = url2Hash.get(new AsciiString(url));

		if (base64hash != null) {
			return base64hash.getBytes();
		}

		int retries = 0;
		final HashKey hk = new HashKey();

		// loop if hash already being used

		while (hash2Url.containsKey(hk)) {

			retries++;
			if (retries > 10) {
				// give up
				logger.warn("Giving up rehashing " + url);
				return null;
			} else if (retries > 1) {
				logger.warn("Rehashing " + url + " / " + retries + "try.");
			}

			hk.rehash();

		}
		
		storeHash(hk, url);
		
		try {
			resumeLog.write(hk.toString() + "," + url
					+ System.getProperty("line.separator"));
			resumeLog.flush();

		} catch (IOException e) {

			logger.error("Could not write to the resume log :(");

			hash2Url.remove(hk);
			url2Hash.remove(url);

			return null;
		}

		return hk.getBytes();
	}

	/**
	 * Strategy to identify repeated URLs more easily. Javascript also does
	 * something similar but can't trust input to be correct.
	 * 
	 * @param url
	 * @return
	 */
	private String normalizeUrl(String url) {

		// normalize Url ending

		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}

		// make sure scheme, domain and TLD are lower case
		
		final int idx = url.indexOf("/"); // first path separator
		
		if (idx == -1) {
			
			// do whole Url
			url = url.toLowerCase();
			
		} else {
			url = url.substring(0, idx).toLowerCase() + url.substring(idx);
		}
		
		// add http:// scheme if needed

		if (!url.startsWith("http://") || !url.startsWith("https://")) {
			url = url.substring("http://".length());
		}
		
		return url;
	}

	/**
	 * get redirect based on hashkey
	 * 
	 * @param filename
	 * @return
	 */
	public StaticPageLet get(final String filename) {

		return hash2Url.get(new HashKey(filename.getBytes()));
	}

	/**
	 * Stop database logging 
	 * 
	 * @throws IOException
	 */
	public void stop() throws IOException {

		logger.trace("Stopping database.");

		resumeLog.flush();
		resumeLog.close();

	}

}
