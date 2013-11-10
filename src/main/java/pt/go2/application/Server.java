package pt.go2.application;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.fileio.Configuration;

import com.sun.net.httpserver.HttpServer;

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

		final HttpServer listener;
		try {

			listener = HttpServer.create(config.HOST, config.BACKLOG);

		} catch (IOException e) {
			logger.fatal("Could not create listener.");
			return;
		}

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

		final VirtualFileSystem vfs = VirtualFileSystem.create(config);

		if (vfs == null) {
			return;
		}

		// RequestHandler

		final RequestHandler requestHandler;
		requestHandler = new RequestHandler(config, vfs, accessLog);

		try {

			listener.createContext("/", requestHandler);
			listener.setExecutor(null);

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

			listener.stop(1);

		} finally {
			try {
				if (accessLog != null) {
					accessLog.close();
				}
			} catch (IOException e) {
			}
			logger.trace("Server stopped.");
		}
	}
}
