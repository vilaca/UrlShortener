package pt.go2.application;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BytesContentProvider;
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

	public void test(Set<Uri> uris) {

		final List<Uri> lst = new ArrayList<>(100);
		StringBuilder sb = new StringBuilder();
		int counter = 0;

		for (Uri uri : uris) {

			if (uri.health() != Health.OK) {
				continue;
			}

			test(uri, false);

			if (uri.health() != Health.OK) {
				logger.trace(uri.toString() + " - " + uri.health().toString());
				continue;
			}

			if (!canUseSafeBrowsingLookup()) {
				continue;
			}

			lst.add(uri);
			sb.append(uri.toString());
			sb.append("\n");
			counter++;

			if (counter == 500) {

				sb.insert(0, "500\n");

				final String list = sb.toString();

				counter = 0;
				sb = new StringBuilder();

				String response = safeBrowsingLookup(list);

				if (response == null) {
					continue;
				}

				final String[] lines = response.split("\n");

				response = null;

				for (int i = 0; i < lines.length; i++) {

					if (lines[i].contains("malware")) {

						lst.get(i).setHealth(Health.MALWARE);

					} else if (lines[i].contains("phishing")) {

						lst.get(i).setHealth(Health.PHISHING);
					}
				}

				lst.clear();
			}
		}
	}

	public void test(Uri uri, boolean useSafeBrowsing) {

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

		if (useSafeBrowsing && canUseSafeBrowsingLookup()) {
			safeBrowsingLookup(uri);
		}
	}

	private boolean canUseSafeBrowsingLookup() {
		return conf.SAFE_LOOKUP_API_KEY != null && !conf.SAFE_LOOKUP_API_KEY.isEmpty();
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

		final HttpClient httpClient = createHttpClient(lookup);

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

	private HttpClient createHttpClient(final String lookup) {

		final HttpClient httpClient;

		if (lookup.startsWith("https://")) {
			httpClient = new HttpClient(new SslContextFactory());
		} else {
			httpClient = new HttpClient();
		}

		httpClient.setFollowRedirects(false);

		return httpClient;
	}

	private String safeBrowsingLookup(String body) {

		final String lookup = "https://sb-ssl.google.com/safebrowsing/api/lookup?client=go2pt&appver=1.0.0&pver=3.1&key="
				+ conf.SAFE_LOOKUP_API_KEY;

		final HttpClient httpClient = createHttpClient(lookup);

		try {

			final ContentResponse httpResponse = httpClient.POST(lookup).content(new BytesContentProvider(body)).send();

			final int r = httpResponse.getStatus();

			switch (r) {
			case 204:
				// no issues found
				return null;
			case 400:
			case 401:
			case 503:
				logger.error("Error " + r + " in POST safebrowsing lookup API.");
				return null;
			}

			final String response = httpResponse.getContentAsString();

			if (response.contains("malware") || response.contains("phishing")) {
				return response;
			}

		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			logger.error("Error in POST safebrowsing lookup API.", e);
		}

		return null;
	}
}
