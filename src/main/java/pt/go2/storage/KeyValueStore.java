package pt.go2.storage;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.fileio.Backup;
import pt.go2.fileio.Restore;
import pt.go2.fileio.RestoreItem;

/**
 * Handle mappings on URLs to HASHED keys and vice versa
 */
public class KeyValueStore {

    private static final int MAX_HASHING_RETRIES = 10;

    private static final Logger LOGGER = LogManager.getLogger(KeyValueStore.class);

    private final BidiMap<HashKey, Uri> map = new BidiMap<HashKey, Uri>();

    // log for restoring hash->url
    private final Backup backupFile;

    public KeyValueStore(String dbFolder) throws IOException {

        this.backupFile = new Backup(dbFolder);

        final List<RestoreItem> restoredItems = Restore.start(dbFolder);

        for (final RestoreItem item : restoredItems) {
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
    public synchronized boolean add(final Uri uri) {

        int retries = 0;
        HashKey hk = new HashKey();

        // loop if hash already being used

        while (map.contains(hk)) {

            retries++;
            if (retries > MAX_HASHING_RETRIES) {
                // give up
                LOGGER.warn("Giving up rehashing " + uri);
                return false;
            } else if (retries > 1) {
                LOGGER.warn("Rehashing " + uri + " / " + retries + "try.");
            }

            hk = new HashKey();
        }

        try {

            final StringBuilder sb = new StringBuilder();

            sb.append(hk.toString());
            sb.append(",");
            sb.append(uri.toString());
            sb.append(System.getProperty("line.separator"));

            backupFile.write(sb.toString());

        } catch (final IOException e) {

            LOGGER.error("Could not write to the resume log.", e);

            return false;
        }

        map.put(hk, uri);

        return true;
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

    public Set<Uri> uris() {
        return map.getKeys();
    }

    public HashKey find(Uri uri) {
        return map.getUrl2Hash(uri);
    }
}
