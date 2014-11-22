package pt.go2.external;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import pt.go2.storage.Uri;
import pt.go2.storage.Uri.Health;

public class SafeBrowsingLookup {

	private static final Logger LOGGER = LogManager.getLogger();

	private final String apiKey;

	public SafeBrowsingLookup(String apiKey) {
		this.apiKey = apiKey;
	}

	public void markBadUris(final List<Uri> lookuplist, String[] response) {
		for (int j = 0; j < response.length; j++) {

			if (response[j].contains("malware")) {

				final Uri uri = lookuplist.get(j);

				uri.setHealth(Health.MALWARE);

				LOGGER.trace("Uri: " + uri.toString() + " H: " + uri.health().toString());

			} else if (response[j].contains("phishing")) {

				final Uri uri = lookuplist.get(j);

				uri.setHealth(Health.PHISHING);

				LOGGER.trace("Uri: " + uri.toString() + " H: " + uri.health().toString());
			}
		}
	}

	public boolean canUseSafeBrowsingLookup() {
		return apiKey != null && !apiKey.isEmpty();
	}

	public void safeBrowsingLookup(Uri uri) {

		final String lookup;

		try {

			final StringBuffer sb = new StringBuffer();

			sb.append("https://sb-ssl.google.com/safebrowsing/api/lookup?client=go2pt&appver=1.0.0&pver=3.1&key=");
			sb.append(apiKey);
			sb.append("&url=");
			sb.append(URLEncoder.encode(uri.toString(), "ASCII"));

			lookup = sb.toString();

		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Error: " + uri, e);
			return;
		}

		final ContentResponse response;
		try {
			final HttpClient httpClient = createHttpClient(lookup);
			response = httpClient.GET(lookup);

		} catch (Exception e) {
			LOGGER.error("Connecting to : " + lookup, e);
			return;
		}

		LOGGER.info("Google SB Lookup API returns " + response.getStatus() + " for " + uri.toString());

		if (response.getStatus() != 200) {
			return;
		}

		if (response.getContentAsString().contains("malware")) {
			uri.setHealth(Health.MALWARE);
		} else {
			uri.setHealth(Health.PHISHING);
		}

		LOGGER.trace("Uri: " + uri.toString() + " H: " + uri.health().toString());
	}

	public String[] safeBrowsingLookup(String body) {

		final String lookup = "https://sb-ssl.google.com/safebrowsing/api/lookup?client=go2pt&appver=1.0.0&pver=3.1&key="
				+ apiKey;

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
				LOGGER.error("Error " + r + " in POST safebrowsing lookup API.");
				return null;
			}

			final String response = httpResponse.getContentAsString();

			if (response.contains("malware") || response.contains("phishing")) {
				return response.split("\n");
			}

		} catch (Exception e) {
			LOGGER.error("Error in POST safebrowsing lookup API.", e);
		}

		return null;
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

}
