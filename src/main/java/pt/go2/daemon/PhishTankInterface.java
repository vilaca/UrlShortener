package pt.go2.daemon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.TruncatedChunkException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	public static PhishTankInterface create(Configuration config, BannedUrlList banned) {

		if (config.PHISHTANK_API_KEY == null) {
			return null;
		}

		return new PhishTankInterface(config.PHISHTANK_API_KEY, banned);
	}

	/**
	 * Refresh banned list
	 */
	@Override
	public void refresh() {

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

		logger.info("Download starting");

		final CloseableHttpClient httpclient = HttpClients.createDefault();

		final HttpGet httpGet = new HttpGet(API_URL);

		final Set<Uri> banned = new HashSet<>(EXPECTED_ENTRIES);

		CloseableHttpResponse response = null;

		try {

			response = httpclient.execute(httpGet);

			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != 200) {
				logger.error("Error on download: " + statusCode);
				return false;
			}

			final BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			String entry;
			
			 // skip header
			br.readLine();
			
			while ((entry = br.readLine()) != null) {

				int idx = entry.indexOf(',') + 1, end;

				if (entry.charAt(idx) == '"') {
					idx++;
					end = entry.indexOf('"', idx);
				} else {
					end = entry.indexOf(',', idx);
				}

				if ( idx == -1 || end == -1 )
				{
					logger.error("Bad entry: " + entry);
					continue;
				}
				
				final Uri uri;
				uri = Uri.create(entry.substring(idx, end), false);

				banned.add(uri);
			}

			this.banned.set(banned);
			
		} catch (IOException e) {

			logger.error(e);
			return false;
			
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				logger.error(e);
				return false;
			}
		}

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
