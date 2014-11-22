package pt.go2.fileio;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pt.go2.application.Resources;
import pt.go2.response.AbstractResponse;
import pt.go2.response.GzipResponse;

public class EmbeddedFiles implements FileSystemInterface {

	final Map<String, AbstractResponse> pages = new HashMap<>();

	public EmbeddedFiles(Configuration config) throws IOException {

		final byte[] index, ajax, robots, map, css;

		index = SmartTagParser.read(Resources.class.getResourceAsStream("/index.html"), config);

		ajax = SmartTagParser.read(Resources.class.getResourceAsStream("/ajax.js"), config);

		robots = SmartTagParser.read(Resources.class.getResourceAsStream("/robots.txt"), config);

		map = SmartTagParser.read(Resources.class.getResourceAsStream("/map.txt"), config);

		css = SmartTagParser.read(Resources.class.getResourceAsStream("/screen.css"), config);

		this.pages.put("/", new GzipResponse(index, AbstractResponse.MIME_TEXT_HTML));

		this.pages.put("ajax.js", new GzipResponse(ajax, AbstractResponse.MIME_APP_JAVASCRIPT));

		this.pages.put("robots.txt", new GzipResponse(robots, AbstractResponse.MIME_TEXT_PLAIN));

		this.pages.put("sitemap.xml", new GzipResponse(map, AbstractResponse.MIME_TEXT_XML));

		this.pages.put("screen.css", new GzipResponse(css, AbstractResponse.MIME_TEXT_CSS));

		if (!config.getGoogleVerification().isEmpty()) {
			this.pages.put(config.getGoogleVerification(),
					new GzipResponse(("google-site-verification: " + config.getGoogleVerification()).getBytes(),
							AbstractResponse.MIME_TEXT_PLAIN));
		}

		// check if all pages created

		for (String page : this.pages.keySet()) {

			final AbstractResponse response = this.pages.get(page);

			if (response == null) {

				throw new IOException("Failed to load page " + page);
			}
		}
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public AbstractResponse getFile(String filename) {
		return pages.get(filename);
	}
}
