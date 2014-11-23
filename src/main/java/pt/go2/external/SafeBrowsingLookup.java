package pt.go2.external;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import pt.go2.storage.Uri;
import pt.go2.storage.Uri.Health;

public class SafeBrowsingLookup {

	private static final String PHISHING = "phishing";

	private static final String MALWARE = "malware";

	private static final Logger LOGGER = LogManager.getLogger();

	private final String apiKey;

	public SafeBrowsingLookup(String apiKey) {
		this.apiKey = apiKey;
	}

	public boolean canUseSafeBrowsingLookup() {
		return apiKey != null && !apiKey.isEmpty();
	}

	public void safeBrowsingLookup(Uri uri) {

		final String lookup;

		try {

			final StringBuilder sb = new StringBuilder();

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

		if (response.getStatus() != HttpStatus.OK_200) {
			return;
		}

		if (response.getContentAsString().contains(MALWARE)) {
			uri.setHealth(Health.MALWARE);
		} else {
			uri.setHealth(Health.PHISHING);
		}

		logBadUri(uri);
	}

	public void safeBrowsingLookup(final List<Uri> lookuplist) {
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

			String[] response = lookup(sb.toString());

			if (response.length == 0) {
				continue;
			}

			markBadUris(lookuplist, response);
		}
	}

	private void markBadUris(final List<Uri> lookuplist, String[] response) {
		for (int j = 0; j < response.length; j++) {

			if (response[j].contains(MALWARE)) {

				final Uri uri = lookuplist.get(j);

				uri.setHealth(Health.MALWARE);

				logBadUri(uri);

			} else if (response[j].contains(PHISHING)) {

				final Uri uri = lookuplist.get(j);

				uri.setHealth(Health.PHISHING);

				logBadUri(uri);
			}
		}
	}

	private void logBadUri(final Uri uri) {
		LOGGER.trace("Uri: " + uri.toString() + " H: " + uri.health().toString());
	}

	private String[] lookup(String body) {

		final String lookup = "https://sb-ssl.google.com/safebrowsing/api/lookup?client=go2pt&appver=1.0.0&pver=3.1&key="
				+ apiKey;

		try {

			final HttpClient httpClient = createHttpClient(lookup);

			final ContentResponse httpResponse = httpClient.POST(lookup)
					.content(new BytesContentProvider(body.getBytes()), "text/plain").send();

			final int r = httpResponse.getStatus();

			switch (r) {
			case HttpStatus.OK_200:
				LOGGER.info("Some issues...");
				break;
			case HttpStatus.NO_CONTENT_204:
				LOGGER.info("No issues...");
				return new String[0];
			case HttpStatus.BAD_REQUEST_400:
			case HttpStatus.UNAUTHORIZED_401:
			case HttpStatus.SERVICE_UNAVAILABLE_503:
			default:
				LOGGER.error("Error " + r + " in POST safebrowsing lookup API.");
				return new String[0];
			}

			return prepareResponse(httpResponse);

		} catch (Exception e) {
			LOGGER.error("Error in POST safebrowsing lookup API.", e);
			return new String[0];
		}
	}

	private String[] prepareResponse(final ContentResponse httpResponse) {

		final String response = httpResponse.getContentAsString();

		if (response.contains(MALWARE) || response.contains(PHISHING)) {
			return response.split("\n");
		}

		return new String[0];
	}

	private HttpClient createHttpClient(final String lookup) throws IOException {

		final HttpClient httpClient;

		if (lookup.startsWith("https://")) {
			httpClient = new HttpClient(new SslContextFactory());
		} else {
			httpClient = new HttpClient();
		}

		httpClient.setFollowRedirects(false);

		try {
			httpClient.start();
		} catch (Exception e) {
			LOGGER.error(e);
			throw new IOException(e.getMessage());
		}

		return httpClient;
	}
}
