/*
    Copyright (C) 2016 João Vilaça

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
*/
package pt.go2.fileio;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Process configuration
 */
public class Configuration {

    private static final Logger LOGGER = LogManager.getLogger();

    // resource file locations on JAR
    private static final String PROPERTIES = "application.properties";

    // Amount of time static pages should be cached
    private final int cacheHint;

    // hash->uri restore folder
    private final String dbFolder;

    // site domain
    private final List<String> domain;

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

        final Properties prop = new Properties();

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

        host = createInetSocketAddress(prop);
        version = getProperty(prop, "server.version");
        cacheHint = getPropertyAsInt(prop, "server.cache");
        redirect = getPropertyAsInt(prop, "server.redirect");

        dbFolder = getResumeFolder(prop);
        domain = getPropertyAsList(prop, "server.domain");

        googleVerification = getProperty(prop, "google-site-verification");
        phishtankApiKey = getProperty(prop, "phishtank-api-key");
        safeLookupApiKey = getProperty(prop, "safe-lookup-api-key");

        watchdogWait = getPropertyAsInt(prop, "watchdog.wait");
        watchdogInterval = getPropertyAsInt(prop, "watchdog.interval");
    }

    private List<String> getPropertyAsList(Properties prop, String key) {

        final String value = getProperty(prop, key);

        if (value != null) {

            final String[] values = value.split(",");

            return Collections.unmodifiableList(Arrays.asList(values));
        }

        return Collections.emptyList();
    }

    /**
     * Use this method only for Smart Tag parsing
     * 
     * @param prop
     *
     * @param string
     *
     * @return
     */
    public String getProperty(Properties prop, String key) {
        String value = prop.getProperty(key);
        if (value != null) {
            value = value.trim();

            if (value.isEmpty()) {
                return null;
            }
        }
        return value;
    }

    private String getResumeFolder(Properties prop) {

        final String resumeFolder = prop.getProperty("database.folder");

        if (resumeFolder == null) {
            return "";
        }

        if (resumeFolder.endsWith(System.getProperty("file.separator"))) {
            return resumeFolder;
        }

        return resumeFolder + System.getProperty("file.separator");
    }

    private InetSocketAddress createInetSocketAddress(Properties prop) {

        // both parameters are optional

        final String addr = prop.getProperty("server.host");
        final int port = getPropertyAsInt(prop, "server.port");

        return addr == null ? new InetSocketAddress(port) : new InetSocketAddress(addr, port);
    }

    private int getPropertyAsInt(Properties prop, String property) {
        return Integer.parseInt(prop.getProperty(property));
    }

    public int getCacheHint() {
        return cacheHint;
    }

    public String getDbFolder() {
        return dbFolder;
    }

    public List<String> getValidDomains() {
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
