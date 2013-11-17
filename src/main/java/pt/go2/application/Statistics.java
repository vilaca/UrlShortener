package pt.go2.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Statistics {

	static class Hit {

		public final String requested;
		public final String referer;
		public final Date date;

		Hit(final String requested, final String referer, final Date date) {

			this.requested = requested;
			this.referer = referer;
			this.date = date;
		}

		@Override
		public String toString() {

			StringBuilder sb = new StringBuilder();

			sb.append("{");

			sb.append(" \"requested\": \"");
			sb.append(requested);

			sb.append("\", \"referer\": \"");
			sb.append(referer);

			sb.append("\", \"date\": \"");
			sb.append(date);

			sb.append("\" }");

			return sb.toString();
		}
	}

	private static final SimpleDateFormat longDate = new SimpleDateFormat(
			"yyyyMMddHHmmss");

	private static final Logger logger = LogManager.getLogger(Statistics.class);

	private final List<Hit> hits = new ArrayList<>();
	private final String folder;

	private FileWriter file;
	private int dayOfWeek;

	public Statistics(final String folder) throws IOException {

		final Calendar date = Calendar.getInstance();

		final String filename = folder + File.separator
				+ new SimpleDateFormat("yyyyMMdd").format(date);

		if (new File(filename).exists()) {
			restore(hits, filename);
		}

		this.file = new FileWriter(filename, true);
		this.folder = folder;
		this.dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
	}

	public void add(final String requested, final String referer,
			final Date date) {

		hits.add(new Hit(requested, referer, date));

		final int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

		if (this.dayOfWeek != dayOfWeek) {

			this.dayOfWeek = dayOfWeek;

			final String filename = this.folder + File.separator
					+ new SimpleDateFormat("yyyyMMdd").format(date);

			hits.clear();

			if (new File(filename).exists()) {
				restore(hits, filename);
			}

			try {
				this.file.close();
				this.file = new FileWriter(filename, true);
			} catch (IOException e) {
			}

		}

		final StringBuffer sb = new StringBuffer();

		sb.append(requested);
		sb.append(" ");
		sb.append(longDate.format(date));
		sb.append(" ");
		sb.append(referer);

		try {
			file.write(sb.toString());
			file.flush();
		} catch (IOException e) {
			logger.warn("Error writing statistics to disk.");
		}
	}

	public List<Hit> getAll() {
		return Collections.unmodifiableList(hits);
	}

	public String getLast24Hours() {

		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR_OF_DAY, -24);

		final Date limit = calendar.getTime();

		return getTimePeriod(limit);
	}

	public String getLastWeek() {

		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -7);

		final Date limit = calendar.getTime();

		return getTimePeriod(limit);
	}

	public String getLastMonth() {

		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);

		final Date limit = calendar.getTime();

		return getTimePeriod(limit);
	}

	private String getTimePeriod(final Date limit) {
		int i = hits.size() - 1;

		final StringBuilder sb = new StringBuilder();

		sb.append("{ \"hits\": [");

		for (;; i--) {
			final Hit hit = hits.get(i);

			sb.append(hit);

			if (hit.date.before(limit) || i == 0)
				break;

			sb.append(", ");
		}

		sb.append("] }");

		return sb.toString();
	}

	static private void restore(final List<Hit> hits, final String filename) {

		final SimpleDateFormat frmt = new SimpleDateFormat("yyyyMMddHHmmss");

		try (final FileReader fr = new FileReader(filename);
				final BufferedReader br = new BufferedReader(fr);) {

			logger.trace("Reading from Resume file: " + filename);

			// read all lines in file

			String line = br.readLine();

			while (line != null) {

				// fields are space separated [ hash key, URI ]

				final String hashkey = line.substring(0, 6);
				final String date = line.substring(7, 14);
				final String referer = line.substring(22);

				try {
					hits.add(new Hit(hashkey, referer, frmt.parse(date)));
				} catch (ParseException e) {
					logger.error("Error parsing date in: " + line);
				}

				// next line
				line = br.readLine();
			}

		} catch (IOException e) {
			logger.error("Error reading: " + filename);
		}
	}
}
