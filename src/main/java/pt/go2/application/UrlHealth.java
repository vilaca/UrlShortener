package pt.go2.application;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;

import pt.go2.abuse.PhishTankInterface;
import pt.go2.fileio.Configuration;
import pt.go2.keystore.KeyValueStore;
import pt.go2.keystore.Uri;
import pt.go2.keystore.Uri.Health;

public class UrlHealth {

	private static final Logger logger = LogManager.getLogger();

	private volatile boolean running = false;

	private final long interval = 60 * 60 * 1000; // 1h

	private final PhishTankInterface pi;
	private final KeyValueStore ks;
	private final Configuration conf;

	public UrlHealth(KeyValueStore ks, PhishTankInterface pi, Configuration conf) {
		this.ks = ks;
		this.pi = pi;
		this.conf = conf;
	}

	public void test() {

		if (running) {
			logger.warn("Already running.");
			return;
		}

		running = true;

		final Set<Uri> uris = ks.Uris();

		final long now = new Date().getTime();

		for (Uri uri : uris) {

			if (uri.health() != Uri.Health.OK) {
				continue;
			}

			if (now - uri.lastChecked() > interval) {
				continue;
			}

			// check if Phishing

			if (pi.isBanned(uri)) {
				uri.setHealth(Uri.Health.PHISHING);
				logger.info("Caugh phishing: " + uri);
				continue;
			}

			final URL url;
			try {
				url = new URL(uri.toString());
			} catch (MalformedURLException e) {
				uri.setHealth(Uri.Health.BAD_URL);
				logger.info("Caugh bad form: " + uri, e);
				continue;
			}

			final HttpURLConnection con;
			try {
				con = (HttpURLConnection) url.openConnection();
			} catch (IOException e) {
				logger.error(e);
				continue;
			}

			con.setInstanceFollowRedirects(false);
			con.setConnectTimeout(5000);

			final int status;
			try {
				con.connect();
				status = con.getResponseCode();
			} catch (IOException e) {
				uri.setHealth(Uri.Health.BAD_URL);
				logger.info("Could not connect or get response code: " + uri, e);
				continue;
			}

			con.disconnect();

			if (status >= 300 && status <= 399) {
				uri.setHealth(Uri.Health.REDIRECT);
				logger.info("Found redirect: " + uri);
				continue;
			}

			if (conf.SAFE_LOOKUP_API_KEY != null && !conf.SAFE_LOOKUP_API_KEY.isEmpty()) {

				safeBrowsingLookup(uri);
			}
		}
		running = false;
	}

	private boolean safeBrowsingLookup(Uri uri) {

		final String lookup;

		try {
			lookup = "https://sb-ssl.google.com/safebrowsing/api/lookup?client="
					+ "go2-pt"
					+ "&key="
					+ conf.SAFE_LOOKUP_API_KEY
					+ "&appver="
					+ conf.VERSION
					+ "&pver=3.1&url="
					+ URLEncoder.encode(uri.toString(), "ASCII");
			
		} catch (UnsupportedEncodingException e) {
			logger.error("Error: " + uri, e);
			return false;
		}

		final HttpClient httpClient = new HttpClient();
		httpClient.setFollowRedirects(false);

		final ContentResponse response;
		try {
			httpClient.start();
			response = httpClient.GET(lookup);
		} catch (Exception e) {
			logger.error("Connecting to : " + lookup, e);
			return false;
		}
		
		logger.info("Google SB Lookup API returns " + response.getStatus() + " for " + uri.toString());
		
		if (response.getStatus() != 200)
			return false;

		if (response.getContentAsString().contains("malware")) {
			uri.setHealth(Health.MALWARE);
		} else {
			uri.setHealth(Health.PHISHING);
		}
		
		return true;
	}
}
