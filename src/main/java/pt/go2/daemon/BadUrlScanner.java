package pt.go2.daemon;

import java.util.Date;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.application.UrlHealth;
import pt.go2.storage.KeyValueStore;
import pt.go2.storage.Uri;

public class BadUrlScanner implements WatchDogTask {

	private static final Logger LOGGER = LogManager.getLogger();

	// watchdog sleep time

	private static final long UPDATE_INTERVAL = 90;

	// url to fetch list from, needs api-key from configuration

	private final KeyValueStore ks;
	private final UrlHealth ul;

	private volatile Date lastRun;

	/**
	 * Factory method - only creates instance if api-key is in configuration
	 * 
	 * @param ul
	 * 
	 * @param config
	 * @return
	 */
	public BadUrlScanner(KeyValueStore ks, UrlHealth ul) {
		this.ks = ks;
		this.ul = ul;
	}

	@Override
	public synchronized void refresh() {

		final Set<Uri> uris = ks.uris();

		LOGGER.info(uris.size() + " total stored uris.");

		ul.test(uris);

		lastRun = new Date();
	}

	@Override
	public Date lastRun() {
		return lastRun;
	}

	@Override
	public long interval() {
		return UPDATE_INTERVAL;
	}

	@Override
	public String name() {
		return "Bad Url Scanner";
	}
}
