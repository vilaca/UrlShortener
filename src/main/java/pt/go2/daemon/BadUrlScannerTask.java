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

import java.util.Date;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.external.UrlHealth;
import pt.go2.storage.KeyValueStore;
import pt.go2.storage.Uri;

public class BadUrlScannerTask implements WatchDogTask {

    private static final Logger LOGGER = LogManager.getLogger();

    // watchdog sleep time

    private static final long UPDATE_INTERVAL = 90;

    // url to fetch list from, needs api-key from configuration

    private final KeyValueStore ks;
    private final UrlHealth ul;

    private volatile Date lastRun;

    /**
     * Factory method - only creates instance if api-key is in configuration
     *
     * @param ul
     *
     * @param config
     * @return
     */
    public BadUrlScannerTask(KeyValueStore ks, UrlHealth ul) {
        this.ks = ks;
        this.ul = ul;
    }

    @Override
    public synchronized void refresh() {

        final Set<Uri> uris = ks.uris();

        LOGGER.info(uris.size() + " total stored uris.");

        ul.test(uris);

        lastRun = new Date();
    }

    @Override
    public Date lastRun() {
        return lastRun == null ? null : new Date(lastRun.getTime());
    }

    @Override
    public long interval() {
        return UPDATE_INTERVAL;
    }

    @Override
    public String name() {
        return "Bad Url Scanner";
    }
}
