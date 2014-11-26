package pt.go2.fileio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Helper class for logging user access file
 *
 */
public class AccessLogger {

    private static final Logger LOGGER = LogManager.getLogger("accesslogger");

    public void log(String logMessage) {
        LOGGER.trace(logMessage);
    }
}
