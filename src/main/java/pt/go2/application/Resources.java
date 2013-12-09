package pt.go2.application;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.daemon.PhishTankInterface;
import pt.go2.daemon.WatchDog;
import pt.go2.fileio.Configuration;
import pt.go2.fileio.EmbeddedFiles;
import pt.go2.fileio.FileSystemInterface;
import pt.go2.fileio.LocalFiles;
import pt.go2.fileio.SmartTagParser;
import pt.go2.keystore.HashKey;
import pt.go2.keystore.KeyValueStore;
import pt.go2.keystore.Uri;
import pt.go2.response.AbstractResponse;
import pt.go2.response.SimpleResponse;
import pt.go2.response.RedirectResponse;

/**
 * Virtualized file resources
 * 
 */
// TODO move error pages out ?
public class Resources {

	/**
	 * Canned responses for errors
	 */

	static final Logger logger = LogManager.getLogger(Resources.class);

	private WatchDog watchdog = new WatchDog();

	// responses
	private FileSystemInterface pages;

	// key = hash / value = uri
	private KeyValueStore ks;

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
			this.ks = new KeyValueStore(config.DATABASE_FOLDER);
		} catch (IOException e) {
			logger.fatal("Could not read backup.");
			return false;
		}

		if (!createErrorPages(config)) {
			logger.fatal("Could not create error pages.");
			return false;
		}

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

	/**
	 * Return error response
	 * 
	 * @param badRequest
	 * @return
	 */
	public AbstractResponse get(Error error) {
		return errors.get(error);
	}

	public boolean isBanned(Uri uri) {

		if (uri.getState() != Uri.State.OK) {
			return true;
		}

		return pi.isBanned(uri);
	}

	public byte[] add(Uri uri) {
		return ks.add(uri);
	}

	public Uri get(HashKey haskey) {
		return ks.get(haskey);
	}
}
