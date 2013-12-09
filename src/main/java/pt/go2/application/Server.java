package pt.go2.application;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.annotations.Page;
import pt.go2.api.HttpsEnforcer;
import pt.go2.fileio.Configuration;

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

				https = createHttps(config);

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
/*
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
*/
			final List<Class<?>> pages = new ArrayList<>();
			
			// scan packages
			
			PageClassLoader.load(config.PAGES_PACKAGES, pages);

			final List<Object> pageObjs = new ArrayList<>();
			
			// instantiate objects and inject dependencies
			
			PageClassLoader.injectDependencies(pages, pageObjs);

			// create contexts
			
			final HttpHandler enforcer = new HttpsEnforcer();

			final boolean usingHttps = https != null
					&& !"no".equals(config.HTTPS_ENABLED);

			for (Class<?> pageClass : pages) {
				
				LOG.info("Creating context for: " + pageClass.getName());

				final Page page = pageClass.getAnnotation(Page.class);
				if (page == null) {
					LOG.info("Missing required Annotations. Skipping");
					continue;
				}

				final HttpHandler handler;
				try {
					handler = (HttpHandler) pageClass.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					LOG.info("Class is not a handler.");
					continue;
				}

				if (usingHttps) {
					https.createContext(page.path(), handler);
					http.createContext(page.path(),
							page.requireLogin() ? enforcer : handler);
				} else {
					https.createContext(page.path(), handler);
				}
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

	private static HttpsServer createHttps(final Configuration config)
			throws KeyStoreException, NoSuchAlgorithmException, IOException,
			CertificateException, FileNotFoundException,
			UnrecoverableKeyException, KeyManagementException {
		HttpsServer https;
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
		return https;
	}

	/**
	 * Private c'tor to forbid instantiation of utility class
	 */
	private Server() {
	}
}
