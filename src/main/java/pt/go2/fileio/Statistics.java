package pt.go2.fileio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.fileio.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Statistics {

	static class Hit {

		public final String ip;
		public final String requested;
		public final String referer;
		public final Date date;

		Hit(String ip, String requested, String referer, Date date) {

			this.ip = ip;
			this.requested = requested;
			this.referer = referer;
			this.date = date;
		}
	}

	private static final Logger LOG = LogManager.getLogger(Statistics.class);

	private static final String FILENAME_MASK = "yyyyMMdd";
	private final List<Hit> hits = new ArrayList<>();

	final Configuration config;

	private FileWriter file;
	private int dayOfWeek;

	public Statistics(final Configuration config) throws IOException {

		final Calendar date = Calendar.getInstance();

		final String filename = config + File.separator
				+ new SimpleDateFormat(FILENAME_MASK).format(date.getTime());

		if (new File(filename).exists()) {
			restore(hits, filename);
		}

		this.file = new FileWriter(filename, true);
		this.config = config;
		this.dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
	}

	public void add(String ip, String requested, String referer, Date date) {

		final ObjectMapper mapper = new ObjectMapper();

		final Hit hit = new Hit(ip, requested, referer, date);

		hits.add(hit);

		final int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

		nextFile(date, dayOfWeek);

		try {
			file.write(mapper.writeValueAsString(hit));
			file.flush();
		} catch (IOException e) {
			LOG.warn("Error writing statistics to disk.", e);
		}
	}

	private void nextFile(Date date, final int dayOfWeek) {

		if (this.dayOfWeek == dayOfWeek) {

			return;
		}

		this.dayOfWeek = dayOfWeek;

		final String filename = this.config.STATISTICS_FOLDER + File.separator
				+ new SimpleDateFormat(FILENAME_MASK).format(date);

		hits.clear();

		if (new File(filename).exists()) {
			restore(hits, filename);
		}

		try {
			this.file.close();
			this.file = new FileWriter(filename, true);
		} catch (IOException e) {
			LOG.error("Error on write.", e);
		}

		try {
			this.file.close();
			this.file = new FileWriter(filename, true);
		} catch (IOException e) {
			LOG.error("Error on close.", e);
		}
	}

	static private void restore(final List<Hit> hits, final String filename) {

		final ObjectMapper mapper = new ObjectMapper();

		try (final FileReader fr = new FileReader(filename);
				final BufferedReader br = new BufferedReader(fr);) {

			LOG.trace("Reading from Resume file: " + filename);

			// read all lines in file

			String line = br.readLine();

			while (line != null) {

				mapper.readValue(line, Hit.class);
				line = br.readLine();
			}

		} catch (IOException e) {
			LOG.error("Error reading: " + filename, e);
		}
	}

	public String getLast24Hours() {
		// TODO Auto-generated method stub
		return null;
	}
}
