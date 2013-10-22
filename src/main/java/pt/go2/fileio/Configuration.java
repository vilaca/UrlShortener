package pt.go2.fileio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.Properties;

public class Configuration {

	// resource file locations on JAR
	private static final String PROPERTIES = "application.properties";
	private static final Properties prop = new Properties();
	
	public final InetSocketAddress host;
	public final int serverBackLog;
	public final String serverAccessLog;
	public final String serverVersion;
	public final int serverRedirect;
	public final String databaseFolder;
	public final String relativePath;
	
	public Configuration ()
	{
		final boolean propertiesOnBaseDir = new File(PROPERTIES).exists();

		// TODO is there a '1.7 way' of getting current path?
		
		try (

		InputStream is = propertiesOnBaseDir ? new FileInputStream(PROPERTIES)
				: Configuration.class.getResourceAsStream("/" + PROPERTIES);) {

			prop.load(is);

		} catch (IOException e) {
		}

		host = createInetSocketAddress();
		serverRedirect = getPropertyAsInt("server.redirect", 301);
		databaseFolder = getResumeFolder();
		
		serverBackLog = getPropertyAsInt("server.backlog", 100);
		serverAccessLog = getProperty("server.accessLog", "access_log");
		serverVersion = getProperty("server.version", "beta");
		
		relativePath = Paths.get("").toAbsolutePath().toString();
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

	public static String getProperty(String string) {
		return prop.getProperty(string);
	}

}
