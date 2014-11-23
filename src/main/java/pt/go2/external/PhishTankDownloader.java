package pt.go2.external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
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

        final HttpClient httpClient = new HttpClient();

        ContentResponse response;
        try {
            httpClient.start();
            response = httpClient.GET(apiUrl);
        } catch (final Exception e) {
            LOGGER.error(e);
            return false;
        }

        final int statusCode = response.getStatus();

        if (statusCode != HttpStatus.OK_200) {
            LOGGER.error("Error on download: " + statusCode);
            return false;
        }

        final BufferedReader br = new BufferedReader(new StringReader(response.getContentAsString()));

        try {

            // skip header
            br.readLine();
            String entry;

            while ((entry = br.readLine()) != null) {

                final Uri uri = parseLineIntoUri(entry);

                if (uri != null) {

                    newList.add(uri);
                }
            }
        } catch (final IOException e) {
            LOGGER.error(e);
            return false;
        } finally {
            try {
                br.close();
            } catch (final IOException e) {
                LOGGER.error(e);
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
