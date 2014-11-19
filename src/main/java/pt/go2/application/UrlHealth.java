package pt.go2.application;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import pt.go2.fileio.Configuration;
import pt.go2.fileio.WhiteList;
import pt.go2.storage.BannedUrlList;
import pt.go2.storage.Uri;
import pt.go2.storage.Uri.Health;

public class UrlHealth {

	private static final Logger logger = LogManager.getLogger();

	private final long interval = 60 * 60 * 1000; // 1h

	private final Configuration conf;
	private final BannedUrlList banned;
	private final WhiteList whitelist;

	public UrlHealth(Configuration conf, WhiteList whitelist, BannedUrlList banned) {

		this.conf = conf;
		this.banned = banned;
		this.whitelist = whitelist;
	}

	public synchronized void test(Uri uri) {

		final long now = new Date().getTime();

		if (now - uri.lastChecked() < interval) {
			return;
		}

		if (this.whitelist.contains(uri.domain())) {
			uri.setHealth(Health.OK);
			return;
		}

		// check if Phishing

		if (banned.isBanned(uri)) {
			uri.setHealth(Uri.Health.PHISHING);
			logger.info("Caugh phishing: " + uri);
			return;
		}

		final URL url;
		try {
			url = new URL(uri.toString());
		} catch (MalformedURLException e) {
			uri.setHealth(Uri.Health.BAD);
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
			uri.setHealth(Uri.Health.BAD);
			logger.info("Could not connect or get response code: " + uri, e);
			return;
		}

		con.disconnect();

		if (status >= 300 && status <= 399) {
			uri.setHealth(Uri.Health.REDIRECT);
			logger.info("Found redirect: " + uri);
			return;
		}

		if (conf.SAFE_LOOKUP_API_KEY != null && !conf.SAFE_LOOKUP_API_KEY.isEmpty()) {

			safeBrowsingLookup(uri);
		}
	}

	private void safeBrowsingLookup(Uri uri) {

		final String lookup;

		try {

			final StringBuffer sb = new StringBuffer();

			sb.append("https://sb-ssl.google.com/safebrowsing/api/lookup?client=go2pt&appver=1.0.0&pver=3.1&key=");
			sb.append(conf.SAFE_LOOKUP_API_KEY);
			sb.append("&url=");
			sb.append(URLEncoder.encode(uri.toString(), "ASCII"));

			lookup = sb.toString();

		} catch (UnsupportedEncodingException e) {
			logger.error("Error: " + uri, e);
			return;
		}

		final HttpClient httpClient;

		if (lookup.startsWith("https://")) {
			httpClient = new HttpClient(new SslContextFactory());
		} else {
			httpClient = new HttpClient();
		}

		httpClient.setFollowRedirects(false);

		final ContentResponse response;
		try {
			httpClient.start();
			response = httpClient.GET(lookup);
		} catch (Exception e) {
			logger.error("Connecting to : " + lookup, e);
			return;
		}

		logger.info("Google SB Lookup API returns " + response.getStatus() + " for " + uri.toString());

		if (response.getStatus() != 200) {
			logger.error("SBAPI http errors: " + response);
			return;
		}

		if (response.getContentAsString().contains("malware")) {
			uri.setHealth(Health.MALWARE);
		} else {
			uri.setHealth(Health.PHISHING);
		}
	}
}
