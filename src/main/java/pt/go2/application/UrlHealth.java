package pt.go2.application;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

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

		final List<Uri> lookuplist = new ArrayList<>();

		for (Uri uri : uris) {

			if (uri.health() != Health.OK) {
				continue;
			}

			test(uri, false);

			if (uri.health() != Health.OK) {
				logger.trace(uri.toString() + " - " + uri.health().toString());
				continue;
			}

			// remember files that still need to be checked

			if (canUseSafeBrowsingLookup()) {
				lookuplist.add(uri);
			}
		}

		// prepare list for safebrowsing lookup

		for (int i = 0; i < lookuplist.size();) {

			// prepare a list of a max of 500 URIs

			final StringBuilder sb = new StringBuilder();

			int j;
			for (j = 0; j < 500 && i < lookuplist.size(); i++, j++) {
				sb.append(lookuplist.get(i).toString());
				sb.append("\n");
			}

			// prepend n of records into list

			sb.insert(0, "\n");
			sb.insert(0, j);

			// response is an array, a entry for each URI

			String[] response = safeBrowsingLookup(sb.toString());

			if (response == null) {
				continue;
			}

			markBadUris(lookuplist, response);
		}
	}

	private void markBadUris(final List<Uri> lookuplist, String[] response) {
		for (int j = 0; j < response.length; j++) {

			if (response[j].contains("malware")) {

				final Uri uri = lookuplist.get(j);

				uri.setHealth(Health.MALWARE);

				logger.trace("Uri: " + uri.toString() + " H: " + uri.health().toString());

			} else if (response[j].contains("phishing")) {

				final Uri uri = lookuplist.get(j);

				uri.setHealth(Health.PHISHING);

				logger.trace("Uri: " + uri.toString() + " H: " + uri.health().toString());
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

		final ContentResponse response;
		try {
			final HttpClient httpClient = createHttpClient(lookup);
			response = httpClient.GET(lookup);

		} catch (Exception e) {
			logger.error("Connecting to : " + lookup, e);
			return;
		}

		logger.info("Google SB Lookup API returns " + response.getStatus() + " for " + uri.toString());

		if (response.getStatus() != 200) {
			return;
		}

		if (response.getContentAsString().contains("malware")) {
			uri.setHealth(Health.MALWARE);
		} else {
			uri.setHealth(Health.PHISHING);
		}

		logger.trace("Uri: " + uri.toString() + " H: " + uri.health().toString());
	}

	private HttpClient createHttpClient(final String lookup) throws Exception {

		final HttpClient httpClient;

		if (lookup.startsWith("https://")) {
			httpClient = new HttpClient(new SslContextFactory());
		} else {
			httpClient = new HttpClient();
		}

		httpClient.setFollowRedirects(false);

		httpClient.start();

		return httpClient;
	}

	private String[] safeBrowsingLookup(String body) {

		final String lookup = "https://sb-ssl.google.com/safebrowsing/api/lookup?client=go2pt&appver=1.0.0&pver=3.1&key="
				+ conf.SAFE_LOOKUP_API_KEY;

		try {

			final HttpClient httpClient = createHttpClient(lookup);

			final ContentResponse httpResponse = httpClient.POST(lookup)
					.content(new BytesContentProvider(body.getBytes()), "text/plain").send();

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
				return response.split("\n");
			}

		} catch (Exception e) {
			logger.error("Error in POST safebrowsing lookup API.", e);
		}

		return null;
	}
}
