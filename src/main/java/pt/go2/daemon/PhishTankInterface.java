package pt.go2.daemon;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;

import pt.go2.fileio.Configuration;
import pt.go2.storage.BannedUrlList;
import pt.go2.storage.Uri;

/**
 * Downloads file from PhishTank API for Phishing Url detection
 */
public class PhishTankInterface implements WatchDogTask {

	private static final Logger logger = LogManager.getLogger();

	// watchdog sleep time

	private static final long UPDATE_INTERVAL = 60;

	// expected entries on API - used to avoid resizing in loop

	private static final int EXPECTED_ENTRIES = 15000;

	// last time the list was refreshed successfully

	private volatile Date lastDownload;

	// url to fetch list from, needs api-key from configuration

	private final String API_URL;

	private final BannedUrlList banned;

	/**
	 * Factory method - only creates instance if api-key is in configuration
	 * 
	 * @param config
	 * @return
	 */
	public static PhishTankInterface create(Configuration config,
			BannedUrlList banned) {

		if (config.PHISHTANK_API_KEY == null) {
			return null;
		}

		return new PhishTankInterface(config.PHISHTANK_API_KEY, banned);
	}

	/**
	 * Refresh banned list
	 */
	@Override
	public synchronized void refresh() {

		if (download()) {
			lastDownload = Calendar.getInstance().getTime();
		}
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
	private boolean download() {

		final Set<Uri> banned = new HashSet<>(EXPECTED_ENTRIES);

		logger.info("Download starting");

		HttpClient httpClient = new HttpClient();

		ContentResponse response;
		try {
			httpClient.start();
			response = httpClient.GET(API_URL);
		} catch (Exception e) {
			logger.error(e);
			return false;
		}

		final int statusCode = response.getStatus();

		if (statusCode != 200) {
			logger.error("Error on download: " + statusCode);
			return false;
		}

		final ByteArrayInputStream ba = new ByteArrayInputStream(
				response.getContent());

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
					logger.error("Bad entry: " + entry);
					continue;
				}

				final Uri uri;
				uri = Uri.create(entry.substring(idx, end), false);

				banned.add(uri);
			}
		} catch (IOException e) {
			logger.error(e);
			return false;
		} finally {
			try {
				ba.close();
			} catch (IOException e) {
			}
		}

		this.banned.set(banned);

		logger.info("Download exiting");

		return true;
	}

	/**
	 * C'tor use factory method instead
	 * 
	 * @param apiKey
	 * @param banned2
	 */
	private PhishTankInterface(final String apiKey, BannedUrlList banned) {

		API_URL = "http://data.phishtank.com/data/" + apiKey
				+ "/online-valid.csv";

		this.banned = banned;
	}

	@Override
	public Date lastRun() {
		return lastDownload;
	}

	@Override
	public long interval() {
		return UPDATE_INTERVAL;
	}

	@Override
	public String name() {
		return "PhishTankTask";
	}
}
