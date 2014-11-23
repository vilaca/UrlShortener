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
import pt.go2.external.PhishLocalCache;
import pt.go2.external.PhishTankDownloader;
import pt.go2.external.SafeBrowsingLookup;
import pt.go2.fileio.Configuration;
import pt.go2.fileio.EmbeddedFiles;
import pt.go2.fileio.ErrorPages;
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

        final Configuration config;

        final KeyValueStore ks;
        final ErrorPages errors;
        final EmbeddedFiles res;

        try {
            config = new Configuration();
            ks = new KeyValueStore(config);
            errors = new ErrorPages();
            res = new EmbeddedFiles(config);

        } catch (final IOException e3) {
            LOGGER.fatal(e3);
            return;
        }

        final WhiteList whitelist = WhiteList.create();
        final PhishLocalCache banned = new PhishLocalCache();
        final PhishTankDownloader phishdl = new PhishTankDownloader(config.getPhishtankApiKey(), banned);
        final SafeBrowsingLookup sbl = new SafeBrowsingLookup(config.getSafeLookupApiKey());

        final UrlHealth ul = new UrlHealth(whitelist, banned, sbl);

        final WatchDog watchdog = new WatchDog();
        final PhishTankInterface pi = new PhishTankInterface(phishdl);
        final BadUrlScanner bad = new BadUrlScanner(ks, ul);

        watchdog.register(pi, true);
        watchdog.register(bad, false);

        watchdog.start(config.getWatchdogWait(), config.getWatchdogInterval());

        LOGGER.trace("Starting server...");

        // log server version

        LOGGER.trace("Preparing to run " + config.getVersion() + ".");

        LOGGER.trace("Resuming DB from folder: " + config.getDbFolder());

        // create listener

        LOGGER.trace("Creating listener.");

        final org.eclipse.jetty.server.Server listener;

        listener = new org.eclipse.jetty.server.Server(config.getHost());

        LOGGER.trace("Appending to access log.");

        // start access log

        BufferedWriter accessLog = null;

        try {
            // TODO
            final FileWriter file = new FileWriter(config.getAccessLog(), true);
            accessLog = new BufferedWriter(file);
        } catch (final IOException e) {

            LOGGER.error("Error creating access log.", e);
        }

        LOGGER.trace("Starting virtual file system.");

        // RequestHandler

        final ContextHandler root = new ContextHandler();
        root.setContextPath("/");
        root.setHandler(new StaticPages(config, accessLog, errors, ks, res));

        final ContextHandler novo = new ContextHandler();
        novo.setContextPath("/new/");
        novo.setHandler(new UrlHashing(config, accessLog, errors, ks, ul));

        final ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { novo, root });

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

        // Destroy server
        try {
            if (accessLog != null) {
                accessLog.close();
            }
        } catch (final IOException e) {

            LOGGER.error("Access log error.", e);
        }

        listener.destroy();

        LOGGER.info("Server stopped.");
    }
}
