package pt.go2.application;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import pt.go2.daemon.BadUrlScanner;
import pt.go2.daemon.PhishTankInterface;
import pt.go2.daemon.WatchDog;
import pt.go2.fileio.Configuration;
import pt.go2.fileio.WhiteList;
import pt.go2.storage.BannedUrlList;
import pt.go2.storage.KeyValueStore;

public class Server {

	static private final Logger logger = LogManager.getLogger(Server.class);

	/**
	 * Process initial method
	 */
	public static void main(final String[] args) {

		final Configuration config = new Configuration();

		final KeyValueStore ks;
		final ErrorPages errors;
		final Resources res;

		try {
			ks = new KeyValueStore(config);
			errors = new ErrorPages();
			res = new Resources(config);

		} catch (IOException e3) {
			logger.fatal(e3);
			return;
		}

		final WhiteList whitelist = WhiteList.create();
		final BannedUrlList banned = new BannedUrlList();

		final UrlHealth ul = new UrlHealth(config, whitelist, banned);

		final WatchDog watchdog = new WatchDog();
		final PhishTankInterface pi = PhishTankInterface.create(config, banned);
		final BadUrlScanner bad = new BadUrlScanner(ks, ul);

		watchdog.register(pi, true);
		watchdog.register(bad, false);

		watchdog.start(config.WATCHDOG_WAIT,config.WATCHDOG_INTERVAL);

		logger.trace("Starting server...");

		// log server version

		logger.trace("Preparing to run " + config.VERSION + ".");

		logger.trace("Resuming DB from folder: " + config.DATABASE_FOLDER);

		// create listener

		logger.trace("Creating listener.");

		final org.eclipse.jetty.server.Server listener;

		listener = new org.eclipse.jetty.server.Server(config.HOST);

		logger.trace("Appending to access log.");

		// start access log

		BufferedWriter accessLog = null;

		try {

			final FileWriter file = new FileWriter(config.ACCESS_LOG, true);
			accessLog = new BufferedWriter(file);
		} catch (IOException e) {
			System.out.println("Access log redirected to console.");
		}

		logger.trace("Starting virtual file system.");

		// RequestHandler

		final ContextHandler root = new ContextHandler();
		root.setContextPath("/");
		root.setHandler(new StaticPages(config, accessLog, errors, ks, res));

		final ContextHandler novo = new ContextHandler();
		novo.setContextPath("/new/");
		novo.setHandler(new UrlHashing(config, accessLog, errors, ks, ul));

		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[] { novo, root });

		listener.setHandler(contexts);

		try {

			// start server

			listener.start();

			logger.trace("Listener is Started.");

			System.out.println("Server Running. Press [k] to kill listener.");
			boolean running = true;
			do {

				try {
					running = System.in.read() != 'k';
				} catch (IOException e) {
				}

			} while (running);

			logger.trace("Server stopping.");

		} catch (Exception e1) {

			logger.trace("Couldn't start server.", e1);

		} finally {

			// Destroy server
			try {
				if (accessLog != null) {
					accessLog.close();
				}
			} catch (IOException e) {
			}

			listener.destroy();
			logger.trace("Server stopped.");
		}
	}
}
