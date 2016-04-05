package pt.go2.fileio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import pt.go2.response.AbstractResponse;
import pt.go2.response.GenericResponse;
import pt.go2.response.GzipResponse;

public class EmbeddedFiles {

	private static final int BUFFER = 4096;

	final Map<String, AbstractResponse> pages = new HashMap<>();
	final Map<String, AbstractResponse> zipped = new HashMap<>();

	public EmbeddedFiles(Configuration config) throws IOException {

		final byte[] index = read(EmbeddedFiles.class.getResourceAsStream("/index.html"));

		final byte[] ajax = read(EmbeddedFiles.class.getResourceAsStream("/ajax.js"));

		final byte[] robots = read(EmbeddedFiles.class.getResourceAsStream("/robots.txt"));

		final byte[] map = read(EmbeddedFiles.class.getResourceAsStream("/sitemap.xml"));

		final byte[] css = read(EmbeddedFiles.class.getResourceAsStream("/screen.css"));

		// zipped
		
		this.zipped.put("/", new GzipResponse(index, AbstractResponse.MIME_TEXT_HTML));

		this.zipped.put("ajax.js", new GzipResponse(ajax, AbstractResponse.MIME_APP_JAVASCRIPT));

		this.zipped.put("robots.txt", new GzipResponse(robots, AbstractResponse.MIME_TEXT_PLAIN));

		this.zipped.put("sitemap.xml", new GzipResponse(map, AbstractResponse.MIME_TEXT_XML));

		this.zipped.put("screen.css", new GzipResponse(css, AbstractResponse.MIME_TEXT_CSS));

		// plain
		
		this.pages.put("/", GenericResponse.create(index, AbstractResponse.MIME_TEXT_HTML));

		this.pages.put("ajax.js", GenericResponse.create(ajax, AbstractResponse.MIME_APP_JAVASCRIPT));

		this.pages.put("robots.txt", GenericResponse.create(robots, AbstractResponse.MIME_TEXT_PLAIN));

		this.pages.put("sitemap.xml", GenericResponse.create(map, AbstractResponse.MIME_TEXT_XML));

		this.pages.put("screen.css", GenericResponse.create(css, AbstractResponse.MIME_TEXT_CSS));

		// google verification for webmaster tools
		
		if (config.getGoogleVerification() != null && !config.getGoogleVerification().isEmpty()) {
			this.zipped.put(config.getGoogleVerification(),
					new GzipResponse(
							("google-site-verification: " + config.getGoogleVerification()).getBytes("US-ASCII"),
							AbstractResponse.MIME_TEXT_PLAIN));
		}

		// check if all pages created

		// TODO is this check required ??
		
		for (final String page : this.zipped.keySet()) {

			final AbstractResponse response = this.zipped.get(page);

			if (response == null) {

				throw new IOException("Failed to load page " + page);
			}
		}
	}

	public AbstractResponse getFile(String filename, boolean compressed) {
		return compressed ? zipped.get(filename) : pages.get(filename);
	}

	private byte[] read(InputStream is) throws IOException {

		final byte[] buffer = new byte[BUFFER];
		int read;

		final ByteArrayOutputStream output = new ByteArrayOutputStream();

		while ((read = is.read(buffer)) != -1) {
			output.write(buffer, 0, read);
		}

		return output.toByteArray();
	}

}
