package pt.go2.application;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import pt.go2.daemon.BadUrlScannerTask;
import pt.go2.daemon.PhishTankInterfaceTask;
import pt.go2.daemon.WatchDog;
import pt.go2.external.PhishLocalCache;
import pt.go2.external.PhishTankDownloader;
import pt.go2.external.SafeBrowsingLookup;
import pt.go2.external.UrlHealth;
import pt.go2.fileio.Configuration;
import pt.go2.fileio.Restore;
import pt.go2.fileio.RestoreItem;
import pt.go2.fileio.WhiteList;
import pt.go2.storage.KeyValueStore;

public class Server {

    private static final Logger LOGGER = LogManager.getLogger(Server.class);

    private Server() {
    }

    /**
     * Process initial method
     */
    public static void main(final String[] args) {

        LOGGER.trace("Starting server...");

        final Configuration config;

        final KeyValueStore ks;
        final EmbeddedFiles res;

        try {
            config = new Configuration();

            final List<RestoreItem> restoredItems = Restore.start(config.getDbFolder());

            ks = new KeyValueStore(restoredItems, config.getDbFolder());
            res = new EmbeddedFiles(config);

        } catch (final IOException ioe) {
            LOGGER.fatal(ioe);
            return;
        }

        final WhiteList whitelist = WhiteList.create();
        final PhishLocalCache banned = new PhishLocalCache();
        final PhishTankDownloader phishdl = new PhishTankDownloader(config.getPhishtankApiKey(), banned);
        final SafeBrowsingLookup sbl = new SafeBrowsingLookup(config.getSafeLookupApiKey());

        final UrlHealth ul = new UrlHealth(whitelist, banned, sbl);

        final WatchDog watchdog = new WatchDog();
        final PhishTankInterfaceTask pi = new PhishTankInterfaceTask(phishdl);
        final BadUrlScannerTask bad = new BadUrlScannerTask(ks, ul);

        watchdog.register(pi, true);
        watchdog.register(bad, false);

        watchdog.start(config.getWatchdogWait(), config.getWatchdogInterval());

        LOGGER.trace("Preparing to run " + config.getVersion() + ".");

        LOGGER.trace("Resuming DB from folder: " + config.getDbFolder());

        // create listener

        LOGGER.trace("Creating listener.");

        final org.eclipse.jetty.server.Server listener;

        listener = new org.eclipse.jetty.server.Server(config.getHost());

        // RequestHandler

        final ContextHandler root = new ContextHandler();
        root.setContextPath("/");
        root.setHandler(new RequestHandler(config, ks, res, ul));

        final ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { root });

        listener.setHandler(contexts);

        try {

            // start server

            listener.start();

        } catch (final Exception e1) {

            LOGGER.error("Server start error.", e1);
            return;
        }

        LOGGER.info("Server Running. Press [k] to kill listener.");

        boolean running = true;
        do {

            try {

                running = System.in.read() != 'k';

            } catch (final IOException e) {

                LOGGER.error(e);
            }

        } while (running);

        LOGGER.trace("Server stopping.");

        listener.destroy();

        LOGGER.info("Server stopped.");
    }
}
