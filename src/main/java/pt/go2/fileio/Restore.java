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
package pt.go2.fileio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.storage.HashKey;

/**
 * Restore Urls and their Hashes
 */
public class Restore {

    private static final Logger LOGGER = LogManager.getLogger(Restore.class);

    private Restore() {
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
            return Collections.emptyList();
        }

        LOGGER.trace("Found " + files.length + " restore files.");

        final List<RestoreItem> items = new ArrayList<RestoreItem>();

        for (final File file : files) {

            // load all data from each file

            try (InputStreamReader isr = new InputStreamReader(new FileInputStream(file.getAbsolutePath()), "US-ASCII");
                    BufferedReader br = new BufferedReader(isr);) {

                LOGGER.trace("Reading from Resume file: " + file.getName());

                // read all lines in file

                String line;

                while ((line = br.readLine()) != null) {

                    // fields are comma separated [ hash key, URI ]

                    final String hashkey = line.substring(0, HashKey.LENGTH);
                    final String uri = line.substring(HashKey.LENGTH + 1);

                    items.add(new RestoreItem(hashkey, uri));
                }

            } catch (final IOException e) {

                LOGGER.error("Error reading: " + file.getAbsolutePath(), e);
            }
        }

        return items;
    }
}
