/*
    Copyright (C) 2016 João Vilaça

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
*/
package pt.go2.daemon;

import java.util.Calendar;
import java.util.Date;

import pt.go2.external.PhishTankDownloader;

/**
 * Downloads file from PhishTank API for Phishing Url detection
 */
public class PhishTankInterfaceTask implements WatchDogTask {

    private final PhishTankDownloader phishTankDownloader;

    // watchdog sleep time

    private static final long UPDATE_INTERVAL = 60;

    // last time the list was refreshed successfully

    private volatile Date lastDownload;

    // url to fetch list from, needs api-key from configuration

    public PhishTankInterfaceTask(PhishTankDownloader phishTankDownloader) {
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
