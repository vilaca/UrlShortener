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

import pt.go2.fileio.Configuration;
import pt.go2.storage.Uri;
import pt.go2.storage.Uri.Health;

public class UrlHealth {

	private static final Logger logger = LogManager.getLogger();

	private final long interval = 60 * 60 * 1000; // 1h
	
	private final Resources vfs;
	private final Configuration conf;

	public UrlHealth(Resources vfs, Configuration conf) {
		this.vfs = vfs;
		this.conf = conf;
	}

	public void test(Set<Uri> uris) {
		for ( Uri uri: uris)
			test(uri);
	}

	public void test(Uri uri) {

		final long now = new Date().getTime();

		if (uri.health() != Health.UNKNOWN && uri.health() != Health.OK) {
			return;
		}

		if (now - uri.lastChecked() < interval) {
			return;
		}

		// check if Phishing

		if (vfs.isBanned(uri)) {
			uri.setHealth(Uri.Health.PHISHING);
			logger.info("Caugh phishing: " + uri);
			return;
		}

		final URL url;
		try {
			url = new URL(uri.toString());
		} catch (MalformedURLException e) {
			uri.setHealth(Uri.Health.BAD_URL);
			logger.info("Caugh bad form: " + uri, e);
			return;
		}

		final HttpURLConnection con;
		try {
			con = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			logger.error(e);
			return;
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
			return;
		}

		con.disconnect();

		if (status >= 300 && status <= 399) {
			uri.setHealth(Uri.Health.REDIRECT);
			logger.info("Found redirect: " + uri);
			return;
		}

		if (conf.SAFE_LOOKUP_API_KEY != null
				&& !conf.SAFE_LOOKUP_API_KEY.isEmpty()) {

			safeBrowsingLookup(uri);
		}
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

		logger.info("Google SB Lookup API returns " + response.getStatus()
				+ " for " + uri.toString());

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
