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
package pt.go2.external;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.storage.Uri;

public class PhishLocalCache {

    private static final Logger LOGGER = LogManager.getLogger();

    // keep the ids off all known entries - supplied by PhishTank
    // and all the banned Uris

    private volatile Set<Uri> banned = new HashSet<>(0);

    public void set(Set<Uri> banned) {

        LOGGER.info("Stats - Old: " + this.banned.size() + " New: " + banned.size());

        this.banned = Collections.unmodifiableSet(banned);
    }

    /**
     * Check if Uri is banned
     *
     * @param uri
     * @return
     */
    public boolean isBanned(final Uri uri) {

        return banned.contains(uri);
    }
}
