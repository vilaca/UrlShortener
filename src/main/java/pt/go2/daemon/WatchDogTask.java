package pt.go2.daemon;

import java.util.Date;

/**
 * Common interface for timed tasks to be executed by Watchdog
 */
public interface WatchDogTask {

	/**
	 * Task to execute
	 */
	void refresh();
	
	/**
	 * When was the task last run?
	 */
	Date lastRun();

	/**
	 * How often is the task to be run, in minutes
	 */
	long interval();
}
