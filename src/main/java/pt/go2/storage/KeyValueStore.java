/*
    Copyright (C) 2016 João Vilaça

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
*/
package pt.go2.storage;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.fileio.Backup;
import pt.go2.fileio.RestoreItem;

/**
 * Handle mappings on URLs to HASHED keys and vice versa
 */
public class KeyValueStore {

    private static final Logger LOGGER = LogManager.getLogger(KeyValueStore.class);

    final Map<String, Uri> hash2Url = new ConcurrentHashMap<>();
    final Map<String, HashKey> url2Hash = new ConcurrentHashMap<>();

    // log for restoring hash->url

    private final Backup backupFile;

    public KeyValueStore(Collection<? extends RestoreItem> restoredItems, String dbFolder) throws IOException {

        this.backupFile = new Backup(dbFolder);

        for (final RestoreItem item : restoredItems) {

            String hk = item.getKey();

            if (!hk.startsWith("/")) {
                hk = "/" + hk;
            }

            hash2Url.put(hk, Uri.create(item.getValue(), false));
        }
    }

    /**
     * Add URL to key store
     *
     * @param uri
     *
     * @return
     */
    public boolean add(final Uri uri) {

        final String hash = findUniqueHash(uri).toString();

        try {

            final StringBuilder sb = new StringBuilder();

            sb.append(hash);
            sb.append(",");
            sb.append(uri.toString());
            sb.append(System.getProperty("line.separator"));

            synchronized (backupFile) {
                backupFile.write(sb.toString());
            }

        } catch (final IOException e) {

            LOGGER.error("Could not write to the resume log.", e);

            // must remove hash
            
            hash2Url.remove(hash);
            
            return false;
        }

        return true;
    }

    private HashKey findUniqueHash(Uri uri) {

        int retries = 0;

        do {
            final HashKey hk = HashKey.create();

            if (hash2Url.putIfAbsent(hk.toString(), uri) == null) {
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

    public Collection<Uri> uris() {
        
        // TODO learn about the performance implications of this method
        
        return hash2Url.values();
    }

    public Uri getUri(String requested) {
        return hash2Url.get(requested);
    }

    public HashKey getHash(Uri uri) {
        return url2Hash.get(uri);
    }
}
