package pt.go2.fileio;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * Process configuration
 */
public class Configuration {

	// resource file locations on JAR
	private static final String PROPERTIES = "application.properties";
	private static final Properties prop = new Properties();
	// apache style access log
	public final String ACCESS_LOG;

	// server listener backlog
	public final int BACKLOG;

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

	// folder for statistics backup/Restore
	public final String STATISTICS_FOLDER;

	// statics login
	public final String STATISTICS_USERNAME;
	public final String STATISTICS_PASSWORD;

	// server version
	public final String VERSION;

	/**
	 * Read configuration from file
	 */
	public Configuration() {
		// attempt reading properties/configuration from JAR

		try (InputStream is = Configuration.class.getResourceAsStream("/"
				+ PROPERTIES);) {
			prop.load(is);

		} catch (IOException e) {
		}

		// attempt reading properties/configuration from basedir

		try (InputStream is = new FileInputStream(PROPERTIES);) {
			prop.load(is);

		} catch (IOException e) {
		}

		ACCESS_LOG = getProperty("server.accessLog", "access_log");
		BACKLOG = getPropertyAsInt("server.backlog", 100);
		CACHE_HINT = getPropertyAsInt("server.cache", 2);
		DATABASE_FOLDER = getResumeFolder();
		ENFORCE_DOMAIN = getProperty("enforce-domain", null);
		GOOGLE_VERIFICATION = getProperty("google-site-verification", "");
		HOST = createInetSocketAddress();
		PHISHTANK_API_KEY = getProperty("phishtank-api-key");
		PUBLIC = getProperty("server.public");
		PUBLIC_ROOT = getProperty("server.public-root");
		REDIRECT = getPropertyAsInt("server.redirect", 301);
		STATISTICS_FOLDER = getProperty("statistics.folder", "");
		STATISTICS_USERNAME = getProperty("statistics.username", "statistics");
		STATISTICS_PASSWORD = getProperty("statistics.password", "secret");
		VERSION = getProperty("server.version", "beta");
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
