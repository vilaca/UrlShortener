package pt.go2.application;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executor;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.fileio.Configuration;
import pt.go2.fileio.Statistics;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

class Server {

	private static final Logger LOG = LogManager.getLogger(Server.class);

	/**
	 * Process initial method
	 */
	public static void main(final String[] args) {

		final Configuration config = new Configuration();

		LOG.trace("Starting server...");

		// log server version

		LOG.trace("Preparing to run " + config.VERSION + ".");

		LOG.trace("Resuming DB from folder: " + config.DATABASE_FOLDER);

		// create listener

		LOG.trace("Creating listener.");

		final HttpServer http;
		try {

			http = HttpServer.create(config.HOST, config.BACKLOG);
		} catch (IOException e) {
			LOG.fatal("Could not create listener.");
			return;
		}

		// Process continues even if Https Listener failed to initialize

		HttpsServer https = null;
		if (config.HOST_HTTPS != null) {
			try {

				final String ksFilename = config.KS_FILENAME;

				final char[] ksPassword = config.KS_PASSWORD;
				final char[] certPassword = config.CERT_PASSWORD;

				final KeyStore ks = KeyStore.getInstance("JKS");
				final SSLContext context = SSLContext.getInstance("TLS");
				final KeyManagerFactory kmf = KeyManagerFactory
						.getInstance("SunX509");

				ks.load(new FileInputStream(ksFilename), ksPassword);
				kmf.init(ks, certPassword);
				context.init(kmf.getKeyManagers(), null, null);

				https = HttpsServer.create(config.HOST_HTTPS, config.BACKLOG);
				https.setHttpsConfigurator(new HttpsConfigurator(context));

			} catch (IOException | KeyStoreException | NoSuchAlgorithmException
					| CertificateException | UnrecoverableKeyException
					| KeyManagementException e) {
				LOG.error("Could not create HTTPS listener.", e);
			}
		}

		// Single Threaded Executor

		final Executor exec = new Executor() {
			@Override
			public void execute(Runnable task) {
				task.run();
			}
		};

		http.setExecutor(exec);

		if (https != null) {
			https.setExecutor(exec);
		}

		LOG.trace("Starting virtual file system.");

		// Generate VFS

		final Resources vfs = new Resources();

		if (!vfs.start(config)) {
			return;
		}

		final Statistics statistics;
		try {
			statistics = new Statistics(config);
		} catch (IOException e1) {
			LOG.fatal("Can't collect statistics.");
			return;
		}

		BufferedWriter accessLog = null;

		try {
			// start access log

			try {

				final FileWriter file = new FileWriter(config.ACCESS_LOG, true);
				accessLog = new BufferedWriter(file);
			} catch (IOException e) {
				System.out.println("Access log redirected to console.");
			}

			LOG.trace("Appending to access log.");

			// RequestHandler

			final BasicAuthenticator ba = new BasicAuthenticator("Statistics") {

				@Override
				public boolean checkCredentials(final String user,
						final String pass) {

					LOG.info("login: [" + user + "] | [" + pass + "]");

					LOG.info("required: [" + config.STATISTICS_USERNAME
							+ "] | [" + config.STATISTICS_PASSWORD + "]");

					return user.equals(config.STATISTICS_USERNAME)
							&& pass.equals(config.STATISTICS_PASSWORD.trim());
				}
			};

			final HttpHandler root = new StaticPages(config, vfs, statistics,
					accessLog);
			final HttpHandler novo = new UrlHashing(config, vfs, accessLog);

			final HttpHandler stats = new Analytics(config, vfs, statistics,
					accessLog);

			final HttpHandler enforcer = new HttpsEnforcer(config, vfs,
					accessLog);

			final HttpHandler reportUrl = new ReportUrl(config, vfs, accessLog,
					vfs);

			final HttpHandler browse = new View(config, vfs, accessLog);

			if (!"no".equals(config.HTTPS_ENABLED) || https == null) {

				http.createContext("/", root);
				http.createContext("/new", novo);
				http.createContext("/report", reportUrl);
				http.createContext("/stats", stats).setAuthenticator(ba);
				http.createContext("/browse", browse).setAuthenticator(ba);

			} else if ("yes".equals(config.HTTPS_ENABLED)) {

				http.createContext("/", root);
				http.createContext("/new", novo);
				https.createContext("/stats", enforcer);
				https.createContext("/browse", enforcer);
				http.createContext("/report", reportUrl);

				https.createContext("/", root);
				https.createContext("/new", novo);
				https.createContext("/stats", stats).setAuthenticator(ba);
				https.createContext("/browse", browse).setAuthenticator(ba);
				https.createContext("/report", reportUrl);

			} else {

				LOG.fatal("Bad parameter in HTTPS config.");
				return;
			}

			// start server

			http.start();
			if (https != null) {
				http.start();
			}

			LOG.trace("Listener is Started.");

			System.out.println("Server Running. Press [k] to kill listener.");
			boolean running = true;
			do {

				try {
					running = System.in.read() == 'k';
				} catch (IOException e) {
				}

			} while (running);

			LOG.trace("Server stopping.");

			http.stop(1);
			if (https != null) {
				http.stop(1);
			}

		} finally {
			try {
				if (accessLog != null) {
					accessLog.close();
				}
			} catch (IOException e) {
			}
			LOG.trace("Server stopped.");
		}
	}

	/**
	 * Private c'tor to forbid instantiation of utility class
	 */
	private Server() {
	}
}
