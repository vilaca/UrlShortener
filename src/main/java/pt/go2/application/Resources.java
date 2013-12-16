package pt.go2.application;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.daemon.PhishTankInterface;
import pt.go2.daemon.WatchDog;
import pt.go2.fileio.Configuration;
import pt.go2.fileio.EmbeddedFiles;
import pt.go2.fileio.FileSystemInterface;
import pt.go2.fileio.LocalFiles;
import pt.go2.keystore.Uri;
import pt.go2.response.AbstractResponse;

/**
 * File resources
 */
public class Resources {

	/**
	 * Canned responses for errors
	 */

	static final Logger logger = LogManager.getLogger(Resources.class);

	private WatchDog watchdog = new WatchDog();

	// responses
	private FileSystemInterface pages;

	private PhishTankInterface pi;

	/**
	 * C'tor
	 * 
	 * @param ks
	 */
	public Resources() {
	}

	public boolean start(final Configuration config) {

		try {
			if (config.PUBLIC == null) {

				pages = new EmbeddedFiles(config);
			} else {

				pages = new LocalFiles(config);
			}

		} catch (IOException e) {
			logger.error("Could load public files.");
			return false;
		}

		pages.start();

		this.pi = PhishTankInterface.create(config);

		if (pi == null) {
			logger.warn("Could init PhishTank API Interface.");
		} else {
			watchdog.register(pi, true);
			watchdog.start(config.WATCHDOG_INTERVAL);
		}

		return true;
	}

	public boolean isBanned(Uri uri) {

		if (uri.getState() != Uri.State.OK) {
			return true;
		}

		return pi.isBanned(uri);
	}

	public List<String> browse() {
		return pages.browse();
	}

	public AbstractResponse get(final String requested) {
		return pages.getFile(requested);
	}
}
