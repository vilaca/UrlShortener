package pt.go2.fileio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import pt.go2.keystore.HashKey;
import pt.go2.keystore.Uri;

/**
 * Persist Urls and their Hashes so they can be later restored w/ the Restore
 * class
 */
public class Backup {

	private final Calendar cal = Calendar.getInstance();

	private final BufferedWriter resumeLog;

	public Backup(final String resumeFolder) throws IOException {

		String filename = generateFilename(resumeFolder);

		while (new File(filename).exists()) {

			try {
				Thread.sleep(7);
			} catch (InterruptedException e) {
			}

			filename = generateFilename(resumeFolder);
		}

		resumeLog = new BufferedWriter(new FileWriter(filename));
	}

	private String generateFilename(final String resumeFolder) {
		return resumeFolder + String.valueOf(cal.getTimeInMillis());
	}

	public void write(final HashKey hk, final Uri uri) throws IOException {

		final StringBuilder sb = new StringBuilder();

		sb.append(hk.toString());
		sb.append(",");
		sb.append(uri.toString());
		sb.append(System.getProperty("line.separator"));

		resumeLog.write(sb.toString());
		resumeLog.flush();
	}

	public void close() throws IOException {

		resumeLog.flush();
		resumeLog.close();
	}
}
