package pt.go2.storage;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.fileio.Backup;
import pt.go2.fileio.Configuration;
import pt.go2.fileio.Restore;
import pt.go2.fileio.RestoreItem;

/**
 * Handle mappings on URLs to HASHED keys and vice versa
 */
public class KeyValueStore {

	private static final Logger LOGGER = LogManager.getLogger(KeyValueStore.class);

	private final BidiMap<HashKey, Uri> map = new BidiMap<HashKey, Uri>();

	// log for restoring hash->url
	private final Backup backupFile;

	public KeyValueStore(final Configuration config) throws IOException {

		this.backupFile = new Backup(config.getDbFolder());

		final List<RestoreItem> restoredItems = Restore.start(config.getDbFolder());

		for (RestoreItem item : restoredItems) {
			final HashKey hk = new HashKey(item.getKey());
			final Uri uri = Uri.create(item.getValue(), false);

			map.put(hk, uri);
		}
	}

	/**
	 * Add URL to key store
	 * 
	 * @param uri
	 * 
	 * @return
	 */
	public synchronized byte[] add(final Uri uri) {

		int retries = 0;
		HashKey hk = new HashKey();

		// loop if hash already being used

		while (map.contains(hk)) {

			retries++;
			if (retries > 10) {
				// give up
				LOGGER.warn("Giving up rehashing " + uri);
				return new byte[0];
			} else if (retries > 1) {
				LOGGER.warn("Rehashing " + uri + " / " + retries + "try.");
			}

			hk = new HashKey();
		}

		map.put(hk, uri);

		try {

			backupFile.write(hk, uri);

		} catch (IOException e) {

			LOGGER.error("Could not write to the resume log :(");

			map.remove(hk, uri);

			return new byte[0];
		}

		return hk.getBytes();
	}

	/**
	 * Close Backup
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		backupFile.close();
	}

	/**
	 * get redirect based on hashkey
	 * 
	 * @param filename
	 * @return
	 */
	public Uri get(final HashKey haskey) {

		return map.get(haskey);
	}

	public Set<Uri> Uris() {
		return map.getKeys();
	}

	public HashKey find(Uri uri) {
		return map.getUrl2Hash(uri);
	}

	public byte[] get(Uri uri) {
		return map.getUrl2Hash(uri).getBytes();
	}
}
