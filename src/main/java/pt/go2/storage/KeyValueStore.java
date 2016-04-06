package pt.go2.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.fileio.Backup;
import pt.go2.fileio.RestoreItem;

/**
 * Handle mappings on URLs to HASHED keys and vice versa
 */
public class KeyValueStore {

    private static final Logger LOGGER = LogManager.getLogger(KeyValueStore.class);

    private final BidiMap<HashKey, Uri> map = new BidiMap<>();

    // log for restoring hash->url

    private final Backup backupFile;

    public KeyValueStore(Collection<? extends RestoreItem> restoredItems, String dbFolder) throws IOException {

        this.backupFile = new Backup(dbFolder);

        for (final RestoreItem item : restoredItems) {

            map.put(new HashKey(item.getKey().getBytes(StandardCharsets.US_ASCII)), Uri.create(item.getValue(), false));
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

        final HashKey hk = findUniqueHash();

        final String hash = hk.toString();

        try {

            final StringBuilder sb = new StringBuilder();

            sb.append(hash);
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

    private HashKey findUniqueHash() {

        int retries = 0;

        do {
            final HashKey hk = HashKey.create();

            if (!map.contains(hk)) {
                return hk;
            }

            LOGGER.warn("Unique hash failed. " + retries + " try.");

        } while (true);
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
