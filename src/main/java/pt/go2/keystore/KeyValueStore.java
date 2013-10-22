package pt.go2.keystore;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.fileio.Backup;
import pt.go2.fileio.Restore;
import pt.go2.fileio.Restore.RestoreItem;
import pt.go2.pagelets.RedirectPageLet;

/**
 * Holds all mappings on URLs to HASHED keys and vice versa
 * 
 */
public class KeyValueStore {

	static final Logger logger = LogManager.getLogger(KeyValueStore.class
			.getName());

	final private BidiMap map = BidiMap.create();
	
	// configurable redirect code
	final private int redirectCode;

	// configurable redirect code
	final private Backup backupFile;

	public static KeyValueStore create(final String resumeFolder,
			final int redirectCode) {
		try {
			return new KeyValueStore(resumeFolder, redirectCode);
		} catch (IOException e) {
			return null;
		}
	}

	private KeyValueStore(final String resumeFolder, final int redirectCode)
			throws IOException {
		final List<RestoreItem> restoredItems = Restore.start(resumeFolder);

		for (RestoreItem item : restoredItems) {
			final HashKey hk = new HashKey(item.Key);
			final Uri uri = new Uri(item.Value);

			storeHash(hk, uri, false);
		}

		this.redirectCode = redirectCode;
		this.backupFile = new Backup(resumeFolder);
	}

	/**
	 * Add to key store
	 * 
	 * @param hk Hash identifier for link
	 * 
	 * @param uri new Uri
	 * 
	 * @param sync true - add synchronized, false - normal  
	 *
	 * @return
	 */
	private boolean storeHash(final HashKey hk, final Uri uri, boolean sync) {

		RedirectPageLet redirect = new RedirectPageLet(redirectCode,
				uri.toString());

		return map.put(hk, uri, redirect);
	}

	/**
	 * Add URL to key store
	 * 
	 * @param uri
	 * 
	 * @return
	 */
	public byte[] add(Uri uri) {

		// lookup database to see if URL is already there

		HashKey base64hash = map.getUrl2Hash(uri);

		if (base64hash != null) {
			return base64hash.getBytes();
		}

		int retries = 0;
		HashKey hk = new HashKey();

		// loop if hash already being used

		while (map.contains(hk) || storeHash(hk, uri, true)) {

			retries++;
			if (retries > 10) {
				// give up
				logger.warn("Giving up rehashing " + uri);
				return null;
			} else if (retries > 1) {
				logger.warn("Rehashing " + uri + " / " + retries + "try.");
			}

			hk = new HashKey();
		}

		try {

			backupFile.write(hk, uri);

		} catch (IOException e) {

			logger.error("Could not write to the resume log :(");

			map.remove(hk,uri);

			return null;
		}

		return hk.getBytes();
	}

	public void close() throws IOException {
		backupFile.close();
	}

	/**
	 * get redirect based on hashkey
	 * 
	 * @param filename
	 * @return
	 */
	public RedirectPageLet get(final String filename) {

		return map.get(new HashKey(filename));
	}

}
