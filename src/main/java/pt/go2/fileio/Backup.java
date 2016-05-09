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
package pt.go2.fileio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;

/**
 * Persist Urls and their Hashes so they can be later restored w/ the Restore
 * class
 */
public class Backup {

    private final Calendar cal = Calendar.getInstance();

    private final BufferedWriter resumeLog;

    public Backup(final String resumeFolder) throws IOException {

        String filename;

        do {
            filename = generateFilename(resumeFolder);
        } while (new File(filename).exists());

        final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filename), "US-ASCII");

        resumeLog = new BufferedWriter(osw);
    }

    private String generateFilename(final String resumeFolder) {
        return resumeFolder + String.valueOf(cal.getTimeInMillis());
    }

    public void write(String record) throws IOException {

        synchronized (this) {
            resumeLog.write(record);
            resumeLog.flush();
        }
    }

    public void close() throws IOException {

        resumeLog.flush();
        resumeLog.close();
    }
}
