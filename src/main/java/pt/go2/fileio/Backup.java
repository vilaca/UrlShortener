package pt.go2.fileio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

        resumeLog = new BufferedWriter(new FileWriter(filename));
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
