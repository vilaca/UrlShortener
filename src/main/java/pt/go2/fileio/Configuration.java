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

    private static final Logger LOGGER = LogManager.getLogger();

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
    private final String domain;

    // Google validation for webmaster tools site
    private final String googleVerification;

    // listener host
    private final InetSocketAddress host;

    // phishing urls database
    private final String phishtankApiKey;

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
     *
     * @throws IOException
     */
    public Configuration() throws IOException {

        // attempt reading properties/configuration from JAR

        boolean readProperties = false;

        try (final InputStream baseProperties = Configuration.class.getResourceAsStream("/" + PROPERTIES)) {

            prop.load(baseProperties);
            readProperties = true;

        } catch (final IOException e) {

            LOGGER.info("Could not read base properties from jar", e);
        }

        // attempt reading properties/configuration from basedir

        try (InputStream additionalProperties = new FileInputStream(PROPERTIES);) {

            prop.load(additionalProperties);

            LOGGER.info("Read additional properties from current directory.");

        } catch (final IOException e) {

            LOGGER.info("Could not read additonal properties from directory", e);

            if (!readProperties) {
                throw new IOException(e);
            }
        }

        // even if no .properties files were loaded, we still got defaults

        host = createInetSocketAddress();
        accessLog = prop.getProperty("server.accessLog");
        version = prop.getProperty("server.version");
        cacheHint = getPropertyAsInt("server.cache");
        redirect = getPropertyAsInt("server.redirect");

        dbFolder = getResumeFolder();
        domain = getProperty("server.domain");

        googleVerification = getProperty("google-site-verification");
        phishtankApiKey = getProperty("phishtank-api-key");
        safeLookupApiKey = getProperty("safe-lookup-api-key");

        watchdogWait = getPropertyAsInt("watchdog.wait");
        watchdogInterval = getPropertyAsInt("watchdog.interval");
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
        final int port = getPropertyAsInt("server.port");

        return addr == null ? new InetSocketAddress(port) : new InetSocketAddress(addr, port);
    }

    private int getPropertyAsInt(String property) {
        return Integer.valueOf(prop.getProperty(property));
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

    public String getDomain() {
        return domain;
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
