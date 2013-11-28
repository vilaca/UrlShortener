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

	// Apache style access log
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
	public final InetSocketAddress HOST_HTTPS;

	public final String HTTPS_ENABLED;

	public final String MAIL_LINK_URL;
	public final String MAIL_SITE_NAME;

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

	public final String SMTP_OUTBOUND_EMAIL;
	public final String SMTP_SERVER_HOST;
	public final int SMTP_SERVER_PORT;
	public final String SMTP_SERVER_USER;
	public final String SMTP_SERVER_PASSWORD;

	public final String USERS_FOLDER;

	// server version
	public final String VERSION;

	// watchdog sleep interval
	public final long WATCHDOG_INTERVAL;

	public final String KS_FILENAME;
	public final char[] KS_PASSWORD;

	public final char[] CERT_PASSWORD;

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
		CERT_PASSWORD = getProperty("server.cert.pass", "").toCharArray();
		DATABASE_FOLDER = getResumeFolder();
		ENFORCE_DOMAIN = getProperty("enforce-domain", null);
		GOOGLE_VERIFICATION = getProperty("google-site-verification", "");
		HOST = createInetSocketAddress();
		HOST_HTTPS = createInetSocketAddressHttps();
		HTTPS_ENABLED = getProperty("server.https-enabled", "no").toLowerCase();
		KS_FILENAME = getProperty("server.keystore.filename");
		KS_PASSWORD = getProperty("server.keystore.password", "").toCharArray();
		MAIL_LINK_URL = getProperty("mail.link.url");
		MAIL_SITE_NAME = getProperty("mail.site-name");
		PHISHTANK_API_KEY = getProperty("phishtank-api-key");
		PUBLIC = getProperty("server.public");
		PUBLIC_ROOT = getProperty("server.public-root");
		REDIRECT = getPropertyAsInt("server.redirect", 301);
		SMTP_OUTBOUND_EMAIL = getProperty("mail.smtp.outbound-email");
		SMTP_SERVER_HOST = getProperty("mail.smtp.server.host");
		SMTP_SERVER_PORT = getPropertyAsInt("mail.smtp.port", 25);
		SMTP_SERVER_USER = getProperty("mail.smtp.username");
		SMTP_SERVER_PASSWORD = getProperty("mail.smtp.password");
		STATISTICS_FOLDER = getProperty("statistics.folder", "");
		STATISTICS_USERNAME = getProperty("statistics.username", "statistics");
		STATISTICS_PASSWORD = getProperty("statistics.password", "secret");
		USERS_FOLDER = getProperty("users.folder");
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

	private InetSocketAddress createInetSocketAddressHttps() {

		if ("no".equals(prop.getProperty("server.https-enabled", "no")
				.toLowerCase())) {
			return null;
		}

		final String host = prop.getProperty("server.host");
		final int port = 443;

		if (host == null) {
			return new InetSocketAddress(port);
		} else {
			return new InetSocketAddress(host, port);
		}
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
