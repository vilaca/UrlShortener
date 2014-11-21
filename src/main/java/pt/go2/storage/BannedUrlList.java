package pt.go2.storage;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BannedUrlList {

	private static final Logger logger = LogManager.getLogger();

	// keep the ids off all known entries - supplied by PhishTank
	// and all the banned Uris

	private volatile Set<Uri> banned = new HashSet<>(0);

	public void set(Set<Uri> banned) {

		logger.info("Stats - Old: " + this.banned.size() + " New: "
				+ banned.size());

		this.banned = Collections.unmodifiableSet(banned);
	}

	/**
	 * Check if Uri is banned
	 * 
	 * @param uri
	 * @return
	 */
	public boolean isBanned(final Uri uri) {

		return banned.contains(uri);
	}
}
