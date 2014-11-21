package pt.go2.application;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.fileio.Configuration;
import pt.go2.fileio.EmbeddedFiles;
import pt.go2.fileio.FileSystemInterface;
import pt.go2.fileio.LocalFiles;
import pt.go2.response.AbstractResponse;

public class Resources {

	static final Logger logger = LogManager.getLogger(Resources.class);

	private FileSystemInterface pages;

	/**
	 * C'tor
	 * 
	 * @param ks
	 * @throws IOException 
	 */
	public Resources(Configuration config) throws IOException {

		try {
			if (config.PUBLIC == null) {

				pages = new EmbeddedFiles(config);
			} else {

				pages = new LocalFiles(config);
			}

		} catch (IOException e) {
			logger.error("Could load public files.", e);
			throw e;
		}

		pages.start();
	}

	public AbstractResponse get(String requested) {
		return pages.getFile(requested);
	}
}
