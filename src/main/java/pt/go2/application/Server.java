package pt.go2.application;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.fileio.Configuration;

import com.sun.net.httpserver.HttpServer;

class Server {

	static private final Configuration config = new Configuration();
	
	static private final Logger logger = LogManager.getLogger(Server.class);

	static private BufferedWriter accessLog = null;

	/**
	 * Process initial method
	 */
	public static void main(final String[] args) {

		logger.trace("Starting server...");

		// log server version

		logger.trace("Preparing to run " + config.VERSION + ".");

		logger.trace("Resuming DB from folder: " + config.DATABASE_FOLDER);

		// create listener

		logger.trace("Server now starting....");

		final HttpServer listener;
		try {

			listener = HttpServer.create(config.HOST, config.BACKLOG);

		} catch (IOException e) {
			logger.fatal("Could not create listener.");
			return;
		}

		// start access log

		try {
			final FileWriter file = new FileWriter(config.ACCESS_LOG, true);
			accessLog = new BufferedWriter(file);
		} catch (IOException e) {
			System.out.println("Access log redirected to console.");
		}

		// instantiate the almighty RequestHandler
		
		try ( final RequestHandler requestHandler = new RequestHandler(config, accessLog);) {

			listener.createContext("/", requestHandler);
			listener.setExecutor(null);

			// start server

			listener.start();

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

		} catch (IOException ex) {
			logger.fatal("Error reading resources from file." + ex.getMessage());
			return;

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
