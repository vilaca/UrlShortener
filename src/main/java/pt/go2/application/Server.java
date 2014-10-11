package pt.go2.application;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import pt.go2.fileio.Configuration;

class Server {

	static private final Logger logger = LogManager.getLogger(Server.class);

	/**
	 * Process initial method
	 */
	public static void main(final String[] args) {

		final Configuration config = new Configuration();

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

		// Generate VFS

		final Resources vfs = new Resources();

		if (!vfs.start(config)) {
			return;
		}

		// RequestHandler

		final ContextHandler root = new ContextHandler();
		root.setContextPath("/");
		root.setHandler(new StaticPages(config, vfs, accessLog));
		
		final ContextHandler novo = new ContextHandler();
		novo.setContextPath("/new");
		novo.setHandler(new UrlHashing(config, vfs, accessLog));
		
		ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new org.eclipse.jetty.server.Handler[] { root, novo });
        
        listener.setHandler(contexts);
		
		try {

			// start server

			listener.start();

			logger.trace("Listener is Started.");

			System.out.println("Server Running. Press [k] to kill listener.");
			boolean running = true;
			do {

				try {
					running = System.in.read() == 'k';
				} catch (IOException e) {
				}

			} while (running);

			logger.trace("Server stopping.");


		} catch (Exception e1) {

			logger.trace("Couldn't start server.");
			
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
