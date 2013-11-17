package pt.go2.daemon;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * Check watch and execute timed tasks
 */
public class WatchDog extends TimerTask {

	static final Logger logger = LogManager.getLogger(WatchDog.class);

	// watchdog timer

	private final Timer watchdog = new Timer();

	private List<WatchDogTask> tasks = new ArrayList<WatchDogTask>();

	/**
	 * Start service
	 * 
	 * @param refresh
	 *            Refresh interval (minutes)
	 */
	public void start(final long refresh) {

		final long millis = TimeUnit.MINUTES.toMillis(refresh);

		watchdog.schedule(new WatchDog(), millis, millis);
	}

	/**
	 * Stop Service
	 */
	public void stop() {
		watchdog.cancel();
		watchdog.purge();
	}

	synchronized public void register(final WatchDogTask task,
			final boolean runNow) {
		
		//tasks.add(task);

		if (runNow) {
		//	task.refresh();
		}
	}

	/**
	 * Trigger download
	 */
	@Override
	synchronized public void run() {

		for (WatchDogTask wt : tasks) {
			if (trigger(wt)) {
				wt.refresh();
			}
		}
	}

	/**
	 * Calculate if its time to trigger the download
	 * 
	 * @return
	 */
	private boolean trigger(final WatchDogTask wt) {

		final Date lastRun = wt.lastRun();
		
		if (wt.lastRun() == null) {
			return true;
		}

		final long current, diff, left;

		current = Calendar.getInstance().getTimeInMillis();
		diff = current - lastRun.getTime();
		left = wt.interval() - TimeUnit.MILLISECONDS.toMinutes(diff);

		logger.info("Minutes to Refresh: " + (left >= 0 ? left : 0));

		return left <= 0;
	}
}
