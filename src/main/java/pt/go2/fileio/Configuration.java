package pt.go2.fileio;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;

public class Configuration {

	// resource file locations on JAR
	private static final String PROPERTIES = "application.properties";
	private static final Properties prop = new Properties();

	// apache style access log
	public final String ACCESS_LOG;
	
	// server listener backlog
	public final int BACKLOG;
	
	// hash->uri restore folder
	public final String DATABASE_FOLDER;
	
	// site domain
	public final String ENFORCE_DOMAIN;
	
	// Google validation for webmaster tools site
	public final String GOOGLE_VALIDATION;
	
	// listener host
	public final InetSocketAddress HOST;
	
	// phishing urls database
	public final String PHISHTANK_API_KEY;
	
	// redirect status code to be used for short Urls
	public final int REDIRECT;
	
	// server version
	public final String VERSION;
		
	/**
	 * Read configuration from file
	 */
	public Configuration ()
	{
		// attempt reading properties/configuration from JAR
		
		try (InputStream is = Configuration.class.getResourceAsStream("/"+ PROPERTIES);)
		{
			prop.load(is);

		} catch (IOException e) {
		}

		// attempt reading properties/configuration from basedir
		
		try (InputStream is = new FileInputStream(PROPERTIES);)
		{
			prop.load(is);

		} catch (IOException e) {
		}

		HOST = createInetSocketAddress();
		REDIRECT = getPropertyAsInt("server.redirect", 301);
		DATABASE_FOLDER = getResumeFolder();
		
		BACKLOG = getPropertyAsInt("server.backlog", 100);
		ACCESS_LOG = getProperty("server.accessLog", "access_log");
		VERSION = getProperty("server.version", "beta");
		
		GOOGLE_VALIDATION = getProperty("google-site-verification", "");
		
		ENFORCE_DOMAIN = getProperty("enforce-domain", null);
		
		PHISHTANK_API_KEY = getProperty("phishtank-api-key");
	}
	
	/**
	 * Use this method only for Smart Tag parsing
	 *  
	 * @param string
	 * 
	 * @return
	 */
	public static String getProperty(String string) {
		return prop.getProperty(string);
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
		
		final String value = prop.getProperty(key);

		if (value == null) {

			return defaultValue;
		}
		
		return value;
	}
	
	private String getResumeFolder() {

		final String resumeFolder = prop.getProperty("database.folder");

		if (resumeFolder == null) {
			return "";
		} 
		
		if (resumeFolder.endsWith(System.getProperty("file.separator")))
		{
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
