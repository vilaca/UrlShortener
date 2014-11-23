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

    private static final Logger LOGGER = LogManager.getLogger();

    // watchdog timer

    private final Timer timer = new Timer();

    private final List<WatchDogTask> tasks = new ArrayList<WatchDogTask>();

    /**
     * Start service
     *
     * @param wait
     *            wait interval (seconds)
     * @param refresh
     *            Refresh interval (minutes)
     */
    public void start(long wait, long refresh) {

        final long waitms = TimeUnit.SECONDS.toMillis(wait);

        final long refreshms = TimeUnit.MINUTES.toMillis(refresh);

        timer.schedule(this, waitms, refreshms);
    }

    /**
     * Stop Service
     */
    public void stop() {
        timer.cancel();
        timer.purge();
    }

    public synchronized void register(final WatchDogTask task, final boolean runNow) {

        if (task == null) {
            return;
        }

        tasks.add(task);

        if (runNow) {
            task.refresh();
        }

        LOGGER.info("Registering. Total tasks: " + tasks.size());
    }

    /**
     * Trigger download
     */
    @Override
    public synchronized void run() {

        LOGGER.info("Watchdog woke.");

        for (final WatchDogTask wt : tasks) {

            LOGGER.info("Checking task: " + wt.name());

            if (trigger(wt)) {
                wt.refresh();
            }
        }

        LOGGER.info("Watchdog back to sleep.");
    }

    /**
     * Calculate if its time to trigger the download
     *
     * @return
     */
    private boolean trigger(final WatchDogTask wt) {

        final Date lastRun = wt.lastRun();

        LOGGER.info("Last run: " + lastRun);

        if (wt.lastRun() == null) {
            return true;
        }

        final long current, diff, left;

        current = Calendar.getInstance().getTimeInMillis();
        diff = current - lastRun.getTime();
        left = wt.interval() - TimeUnit.MILLISECONDS.toMinutes(diff);

        LOGGER.info("Minutes to Refresh: " + (left >= 0 ? left : 0));

        return left <= 0;
    }
}
