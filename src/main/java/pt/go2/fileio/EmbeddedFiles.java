package pt.go2.fileio;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pt.go2.application.Resources;
import pt.go2.response.AbstractResponse;
import pt.go2.response.GzipResponse;

public class EmbeddedFiles implements FileSystemInterface {

	final private Map<String, AbstractResponse> pages = new HashMap<>();

	public EmbeddedFiles(Configuration config) throws IOException {

		final byte[] index, ajax, robots, map, css;

		index = SmartTagParser.read(Resources.class
				.getResourceAsStream("/index.html"));

		ajax = SmartTagParser.read(Resources.class
				.getResourceAsStream("/ajax.js"));

		robots = SmartTagParser.read(Resources.class
				.getResourceAsStream("/robots.txt"));

		map = SmartTagParser.read(Resources.class
				.getResourceAsStream("/map.txt"));

		css = SmartTagParser.read(Resources.class
				.getResourceAsStream("/screen.css"));

		this.pages.put("/", new GzipResponse(index,
				AbstractResponse.MIME_TEXT_HTML));
		this.pages.put("ajax.js", new GzipResponse(ajax,
				AbstractResponse.MIME_APP_JAVASCRIPT));

		this.pages.put("robots.txt", new GzipResponse(robots,
				AbstractResponse.MIME_TEXT_PLAIN));

		this.pages.put("sitemap.xml", new GzipResponse(map,
				AbstractResponse.MIME_TEXT_XML));

		this.pages.put("screen.css", new GzipResponse(css,
				AbstractResponse.MIME_TEXT_CSS));

		if (!config.GOOGLE_VERIFICATION.isEmpty()) {
			this.pages
					.put(config.GOOGLE_VERIFICATION,
							new GzipResponse(
									("google-site-verification: " + config.GOOGLE_VERIFICATION)
											.getBytes(),
									AbstractResponse.MIME_TEXT_PLAIN));
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
