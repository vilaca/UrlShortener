/**
 * 
 */
package eu.vilaca.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author vilaca
 * 
 */
public class PropertiesManager {

	// resource file locations on JAR
	private static final String PROPERTIES = "application.properties";
	private static final Properties prop = new Properties();

	static {

		boolean propertiesOnBaseDir = new File(PROPERTIES).exists();

		try (

		InputStream is = propertiesOnBaseDir ? new FileInputStream(PROPERTIES)
				: Server.class.getResourceAsStream("/" + PROPERTIES);) {

			prop.load(is);

		} catch (IOException e) {

		}
	}

	public static Properties getProperties() {
		return prop;
	}

}
