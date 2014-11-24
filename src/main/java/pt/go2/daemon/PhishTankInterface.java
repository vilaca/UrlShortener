package pt.go2.daemon;

import java.util.Calendar;
import java.util.Date;

import pt.go2.external.PhishTankDownloader;

/**
 * Downloads file from PhishTank API for Phishing Url detection
 */
public class PhishTankInterface implements WatchDogTask {

    private final PhishTankDownloader phishTankDownloader;

    // watchdog sleep time

    private static final long UPDATE_INTERVAL = 60;

    // last time the list was refreshed successfully

    private volatile Date lastDownload;

    // url to fetch list from, needs api-key from configuration

    public PhishTankInterface(PhishTankDownloader phishTankDownloader) {
        this.phishTankDownloader = phishTankDownloader;
    }

    /**
     * Refresh banned list
     */
    @Override
    public synchronized void refresh() {

        if (this.phishTankDownloader.download()) {
            lastDownload = Calendar.getInstance().getTime();
        }
    }

    @Override
    public Date lastRun() {
        return lastDownload == null ? null : new Date(lastDownload.getTime());
    }

    @Override
    public long interval() {
        return UPDATE_INTERVAL;
    }

    @Override
    public String name() {
        return "PhishTankTask";
    }
}
