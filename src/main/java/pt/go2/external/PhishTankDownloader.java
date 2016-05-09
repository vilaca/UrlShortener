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
package pt.go2.external;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;

import pt.go2.storage.Uri;

public class PhishTankDownloader {

    private static final Logger LOGGER = LogManager.getLogger();

    // expected entries on API - used to avoid resizing in loop

    private static final int EXPECTED_ENTRIES = 15000;

    private final String apiUrl;

    private final PhishLocalCache banned;

    /**
     * C'tor use factory method instead
     *
     * @param apiKey
     * @param banned2
     */
    public PhishTankDownloader(final String apiKey, PhishLocalCache banned) {

        apiUrl = "http://data.phishtank.com/data/" + apiKey + "/online-valid.csv";

        this.banned = banned;
    }

    /**
     * Call API and parse response
     *
     * @return
     *
     * @throws ClientProtocolException
     * @throws IOException
     * @throws TruncatedChunkException
     */
    public boolean download() {

        final Set<Uri> newList = new HashSet<>(EXPECTED_ENTRIES);

        LOGGER.info("Download starting");

        final HttpClientResponse response = HttpClientAdapter.get(apiUrl);

        if (response == null) {
            return false;
        }

        if (response.status() != HttpStatus.OK_200) {
            LOGGER.error("Error on download. " + response.status());
            return false;
        }

        if (response.records().size() <= 1) {
            LOGGER.error("Error on download?? Size is " + response.records().size());
            return false;
        }

        for (int i = 1; i < response.records().size(); i++) {

            final Uri uri = parseLineIntoUri(response.records().get(i));

            if (uri != null) {

                newList.add(uri);
            }
        }

        this.banned.set(newList);

        LOGGER.info("Download exiting");

        return true;
    }

    private Uri parseLineIntoUri(String entry) {

        int idx = entry.indexOf(',') + 1, end;

        if (entry.charAt(idx) == '"') {
            idx++;
            end = entry.indexOf('"', idx);
        } else {
            end = entry.indexOf(',', idx);
        }

        if (idx == -1 || end == -1) {
            LOGGER.error("Bad entry: " + entry);
            return null;
        }

        return Uri.create(entry.substring(idx, end), false);
    }

}
