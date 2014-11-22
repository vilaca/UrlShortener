package pt.go2.external;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;

import pt.go2.storage.Uri;

public class PhishTankDownloader {

	private static final Logger LOGGER = LogManager.getLogger();

	// expected entries on API - used to avoid resizing in loop

	private static final int EXPECTED_ENTRIES = 15000;
	
	private final String API_URL;

	private final PhishLocalCache banned;

	/**
	 * C'tor use factory method instead
	 * 
	 * @param apiKey
	 * @param banned2
	 */
	public PhishTankDownloader(final String apiKey, PhishLocalCache banned) {

		API_URL = "http://data.phishtank.com/data/" + apiKey + "/online-valid.csv";

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

		final Set<Uri> banned = new HashSet<>(EXPECTED_ENTRIES);

		LOGGER.info("Download starting");

		HttpClient httpClient = new HttpClient();

		ContentResponse response;
		try {
			httpClient.start();
			response = httpClient.GET(API_URL);
		} catch (Exception e) {
			LOGGER.error(e);
			return false;
		}

		final int statusCode = response.getStatus();

		if (statusCode != 200) {
			LOGGER.error("Error on download: " + statusCode);
			return false;
		}

		final ByteArrayInputStream ba = new ByteArrayInputStream(response.getContent());

		final BufferedReader br = new BufferedReader(new InputStreamReader(ba));

		try {

			// skip header
			br.readLine();
			String entry;

			while ((entry = br.readLine()) != null) {

				int idx = entry.indexOf(',') + 1, end;

				if (entry.charAt(idx) == '"') {
					idx++;
					end = entry.indexOf('"', idx);
				} else {
					end = entry.indexOf(',', idx);
				}

				if (idx == -1 || end == -1) {
					LOGGER.error("Bad entry: " + entry);
					continue;
				}

				final Uri uri;
				uri = Uri.create(entry.substring(idx, end), false);

				banned.add(uri);
			}
		} catch (IOException e) {
			LOGGER.error(e);
			return false;
		} finally {
			try {
				ba.close();
			} catch (IOException e) {
			}
		}

		this.banned.set(banned);

		LOGGER.info("Download exiting");

		return true;
	}

}
