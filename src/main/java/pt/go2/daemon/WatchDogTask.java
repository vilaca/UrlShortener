package pt.go2.daemon;

import java.util.Date;

/**
 * Common interface for timed tasks to be executed by Watchdog
 */
public interface WatchDogTask {

	void refresh();
	
	Date lastRun();
	
	long interval();
	
}
