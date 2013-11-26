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

		private String ip;
		private String requested;
		private String referer;
		private String date;

		public Hit() {
		}

		public Hit(String ip, String requested, String referer, String date) {

			this.setIp(ip);
			this.setRequested(requested);
			this.setReferer(referer);
			this.setDate(date);
		}

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public String getRequested() {
			return requested;
		}

		public void setRequested(String requested) {
			this.requested = requested;
		}

		public String getReferer() {
			return referer;
		}

		public void setReferer(String referer) {
			this.referer = referer;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
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

		final String filename = config.STATISTICS_FOLDER + File.separator
				+ new SimpleDateFormat(FILENAME_MASK).format(date.getTime());

		if (new File(filename).exists()) {
			restore(hits, filename);
		}

		this.file = new FileWriter(filename, true);
		this.config = config;
		this.dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
	}

	public void add(String ip, String requested, String referer, Date date) {

		final String dateString = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss,SSS Z").format(date);

		if (referer == null) {
			referer = "";
		}

		final ObjectMapper mapper = new ObjectMapper();

		final Hit hit = new Hit(ip, requested, referer, dateString);

		hits.add(hit);

		final int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

		nextFile(date, dayOfWeek);

		try {
			final String line = mapper.writeValueAsString(hit);
			file.write(line);
			file.write(System.getProperty("line.separator"));
			file.flush();
		} catch (IOException e) {
			LOG.warn("Error writing statistics to disk.", e);
		}
	}

	private void nextFile(Date date, final int dayOfWeek) {

		if (this.dayOfWeek == dayOfWeek) {

			return;
		}

		final String filename = this.config.STATISTICS_FOLDER + File.separator
				+ new SimpleDateFormat(FILENAME_MASK).format(date);

		hits.clear();

		if (new File(filename).exists()) {
			restore(hits, filename);
		}

		try {
			this.file = new FileWriter(filename, true);
		} catch (IOException e) {
			LOG.error("Error on file creation.", e);
			return;
		}

		try {
			this.file.close();
		} catch (IOException e) {
			LOG.error("Error on close.", e);
		}

		this.dayOfWeek = dayOfWeek;
	}

	static private void restore(final List<Hit> hits, final String filename) {

		final ObjectMapper mapper = new ObjectMapper();

		try (final FileReader fr = new FileReader(filename);
				final BufferedReader br = new BufferedReader(fr);) {

			LOG.trace("Reading from Resume file: " + filename);

			// read all lines in file

			String line = br.readLine();

			while (line != null) {

				try {
					Hit hit = mapper.readValue(line, Hit.class);
					hits.add(hit);
				} catch (IOException e) {
					LOG.error("Statistics entry problem.", e);
				}
				line = br.readLine();
			}

		} catch (IOException e) {
			LOG.error("Error reading: " + filename, e);
		}
	}

	public List<Hit> getLast24Hours() {
		return hits;
	}
}
