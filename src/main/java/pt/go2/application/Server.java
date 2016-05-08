/*
    Copyright (C) 2016 João Vilaça

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
*/
package pt.go2.application;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import pt.go2.application.EmbeddedPages.Builder;
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
        final EmbeddedPages res;

        try {
            config = new Configuration();

            final List<RestoreItem> restoredItems = Restore.start(config.getDbFolder());

            ks = new KeyValueStore(restoredItems, config.getDbFolder());

            Builder staticPagesBuilder = new EmbeddedPages.Builder();

            // single HTML page
            staticPagesBuilder.add("/index.html", MimeTypeConstants.MIME_TEXT_HTML);

            // javascript ( handles ajax )
            staticPagesBuilder.add("/ajax.js", MimeTypeConstants.MIME_APP_JAVASCRIPT);

            // page style
            staticPagesBuilder.add("/screen.css", MimeTypeConstants.MIME_TEXT_CSS);

            // search engine stuff
            staticPagesBuilder.add("/robots.txt", MimeTypeConstants.MIME_TEXT_PLAIN);
            staticPagesBuilder.add("/sitemap.xml", MimeTypeConstants.MIME_TEXT_XML);

            // root must be redirected to index
            staticPagesBuilder.setAlias("/", "/index.html");

            // create entry required by https://www.google.com/webmasters/tools/home?hl=en
            // to prove site ownership
            if (config.getGoogleVerification() != null && !config.getGoogleVerification().isEmpty()) {

                staticPagesBuilder.add(config.getGoogleVerification(), 
                        ("google-site-verification: " + config.getGoogleVerification())
                        .getBytes(StandardCharsets.US_ASCII), MimeTypeConstants.MIME_TEXT_PLAIN);
            }
            
            // done
            res = staticPagesBuilder.create();

        } catch (final Exception ioe) {
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
            listener.join();

        } catch (final Exception e1) {

            LOGGER.error("Server start error.", e1);
            return;
        }

        // TODO all bellow is unreached
        
        LOGGER.trace("Server stopping.");

        listener.destroy();

        LOGGER.info("Server stopped.");
    }
}
