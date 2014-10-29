package pt.go2.fileio;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Process configuration
 */
public class Configuration {

	static final Logger logger = LogManager.getLogger();
	
	// resource file locations on JAR
	private static final String PROPERTIES = "application.properties";
	private static final Properties prop = new Properties();

	// apache style access log
	public final String ACCESS_LOG;

	// Amount of time static pages should be cached
	public final int CACHE_HINT;

	// hash->uri restore folder
	public final String DATABASE_FOLDER;

	// site domain
	public final String ENFORCE_DOMAIN;

	// Google validation for webmaster tools site
	public final String GOOGLE_VERIFICATION;

	// listener host
	public final InetSocketAddress HOST;

	// phishing urls database
	public final String PHISHTANK_API_KEY;

	public final String PUBLIC;
	public final String PUBLIC_ROOT;

	// redirect status code to be used for short Urls
	public final int REDIRECT;

	// Google safe browsing lookup API key
	public final String SAFE_LOOKUP_API_KEY;
	
	// server version
	public final String VERSION;

	// watchdog sleep interval
	public final long WATCHDOG_INTERVAL;

	/**
	 * Read configuration
	 */
	public Configuration() {

		// attempt reading properties/configuration from JAR

		try (InputStream is = Configuration.class.getResourceAsStream("/"
				+ PROPERTIES);) {
			prop.load(is);
			logger.info("Read embedded properties from jar file.");
		} catch (IOException e) {
			logger.info("Could not read properties from jar");
		}

		// attempt reading properties/configuration from basedir

		try (InputStream is = new FileInputStream(PROPERTIES);) {
			prop.load(is);
			logger.info("Read properties from current directory.");
		} catch (IOException e) {
			logger.info("Could not read properties from directory");
		}

		ACCESS_LOG = getProperty("server.accessLog", "access_log");
		CACHE_HINT = getPropertyAsInt("server.cache", 2);
		DATABASE_FOLDER = getResumeFolder();
		ENFORCE_DOMAIN = getProperty("enforce-domain", null);
		GOOGLE_VERIFICATION = getProperty("google-site-verification", "");
		HOST = createInetSocketAddress();
		PHISHTANK_API_KEY = getProperty("phishtank-api-key");
		PUBLIC = getProperty("server.public");
		PUBLIC_ROOT = getProperty("server.public-root");
		REDIRECT = getPropertyAsInt("server.redirect", 301);
		SAFE_LOOKUP_API_KEY = getProperty("safe-lookup-api-key");
		VERSION = getProperty("server.version", "beta");
		WATCHDOG_INTERVAL = getPropertyAsInt("watchdog.interval", 16);
	}

	/**
	 * Use this method only for Smart Tag parsing
	 * 
	 * @param string
	 * 
	 * @return
	 */
	public static String getProperty(String key) {
		String value = prop.getProperty(key);
		if (value != null) {
			value = value.trim();

			if (value.isEmpty()) {
				return null;
			}
		}
		return value;
	}

	private int getPropertyAsInt(final String key, final int defaultValue) {

		final String value = prop.getProperty(key);

		if (value != null) {
			try {
				return Integer.parseInt(value);

			} catch (NumberFormatException nfe) {
			}
		}

		return defaultValue;
	}

	private String getProperty(final String key, final String defaultValue) {

		final String value = getProperty(key);

		if (value == null || value.isEmpty()) {

			return defaultValue;
		}

		return value;
	}

	private String getResumeFolder() {

		final String resumeFolder = prop.getProperty("database.folder");

		if (resumeFolder == null) {
			return "";
		}

		if (resumeFolder.endsWith(System.getProperty("file.separator"))) {
			return resumeFolder;
		}

		return resumeFolder + System.getProperty("file.separator");
	}

	private InetSocketAddress createInetSocketAddress() {

		// both parameters are optional

		final String host = prop.getProperty("server.host");
		final String port = prop.getProperty("server.port");

		// default port is 80

		final int _port;
		if (port == null) {
			_port = 80;
		} else {
			_port = Integer.parseInt(port);
		}

		if (host == null) {
			return new InetSocketAddress(_port);
		} else {
			return new InetSocketAddress(host, _port);
		}
	}
}
