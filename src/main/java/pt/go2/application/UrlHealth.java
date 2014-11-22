package pt.go2.application;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.external.PhishLocalCache;
import pt.go2.external.SafeBrowsingLookup;
import pt.go2.fileio.WhiteList;
import pt.go2.storage.Uri;
import pt.go2.storage.Uri.Health;

public class UrlHealth {

	private static final Logger LOGGER = LogManager.getLogger();

	private final long interval = 60 * 60 * 1000; // 1h

	private final PhishLocalCache banned;
	private final WhiteList whitelist;
	private final SafeBrowsingLookup sbl;

	public UrlHealth(WhiteList whitelist, PhishLocalCache banned, SafeBrowsingLookup sbl) {
		this.banned = banned;
		this.whitelist = whitelist;
		this.sbl = sbl;
	}

	public void test(Set<Uri> uris) {

		final List<Uri> lookuplist = new ArrayList<>();

		for (Uri uri : uris) {

			if (uri.health() != Health.OK) {
				continue;
			}

			test(uri, false);

			if (uri.health() != Health.OK) {
				LOGGER.trace(uri.toString() + " - " + uri.health().toString());
				continue;
			}

			// remember files that still need to be checked

			if (sbl.canUseSafeBrowsingLookup()) {
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

			String[] response = sbl.safeBrowsingLookup(sb.toString());

			if (response == null) {
				continue;
			}

			sbl.markBadUris(lookuplist, response);
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
			LOGGER.info("Caugh phishing: " + uri);
			return;
		}

		if (useSafeBrowsing && sbl.canUseSafeBrowsingLookup()) {
			sbl.safeBrowsingLookup(uri);
		}
	}
}
