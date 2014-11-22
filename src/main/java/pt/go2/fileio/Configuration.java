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

	private static final Logger LOG = LogManager.getLogger();

	private final Properties prop = new Properties();

	// resource file locations on JAR
	private static final String PROPERTIES = "application.properties";

	// apache style access log
	private final String accessLog;

	// Amount of time static pages should be cached
	private final int cacheHint;

	// hash->uri restore folder
	private final String dbFolder;

	// site domain
	private final String enforceDomain;

	// Google validation for webmaster tools site
	private final String googleVerification;

	// listener host
	private final InetSocketAddress host;

	// phishing urls database
	private final String phishtankApiKey;

	private final String publicRoot;

	// redirect status code to be used for short Urls
	private final int redirect;

	// Google safe browsing lookup API key
	private final String safeLookupApiKey;

	// server version
	private final String version;

	// watchdog sleep interval
	private final long watchdogWait;
	private final long watchdogInterval;

	/**
	 * Read configuration
	 */
	public Configuration() {

		// attempt reading properties/configuration from JAR

		try (InputStream is = Configuration.class.getResourceAsStream("/" + PROPERTIES);) {
			prop.load(is);
			LOG.info("Read embedded properties from jar file.");
		} catch (IOException e) {
			LOG.info("Could not read properties from jar", e);
		}

		// attempt reading properties/configuration from basedir

		try (InputStream is = new FileInputStream(PROPERTIES);) {
			prop.load(is);
			LOG.info("Read properties from current directory.");
		} catch (IOException e) {
			LOG.info("Could not read properties from directory", e);
		}

		// even if no .properties files were loaded, we still got defaults
		
		accessLog = getProperty("server.accessLog", "access_log");
		cacheHint = getPropertyAsInt("server.cache", 2);
		dbFolder = getResumeFolder();
		enforceDomain = getProperty("enforce-domain", null);
		googleVerification = getProperty("google-site-verification", "");
		host = createInetSocketAddress();
		phishtankApiKey = getProperty("phishtank-api-key");
		publicRoot = getProperty("server.public-root");
		redirect = getPropertyAsInt("server.redirect", 301);
		safeLookupApiKey = getProperty("safe-lookup-api-key");
		version = getProperty("server.version", "beta");
		watchdogWait = getPropertyAsInt("watchdog.wait", 5);
		watchdogInterval = getPropertyAsInt("watchdog.interval", 16);
	}

	/**
	 * Use this method only for Smart Tag parsing
	 * 
	 * @param string
	 * 
	 * @return
	 */
	public String getProperty(String key) {
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
				return defaultValue;
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

		final String addr = prop.getProperty("server.host");
		final int port = getPropertyAsInt("server.port", 80);

		return addr == null ? new InetSocketAddress(port) : new InetSocketAddress(addr, port);
	}

	public String getAccessLog() {
		return accessLog;
	}

	public int getCacheHint() {
		return cacheHint;
	}

	public String getDbFolder() {
		return dbFolder;
	}

	public String getEnforceDomain() {
		return enforceDomain;
	}

	public String getGoogleVerification() {
		return googleVerification;
	}

	public String getPhishtankApiKey() {
		return phishtankApiKey;
	}

	public InetSocketAddress getHost() {
		return host;
	}

	public String getPublicRoot() {
		return publicRoot;
	}

	public int getRedirect() {
		return redirect;
	}

	public String getSafeLookupApiKey() {
		return safeLookupApiKey;
	}

	public String getVersion() {
		return version;
	}

	public long getWatchdogWait() {
		return watchdogWait;
	}

	public long getWatchdogInterval() {
		return watchdogInterval;
	}
}
