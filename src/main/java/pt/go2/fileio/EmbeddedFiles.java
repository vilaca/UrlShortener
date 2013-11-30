package pt.go2.fileio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.go2.response.AbstractResponse;
import pt.go2.response.GzipResponse;

public class EmbeddedFiles implements FileSystemInterface {

	final private Map<String, AbstractResponse> pages = new HashMap<>();

	public EmbeddedFiles(Configuration config) throws IOException {

		final byte[] index, ajax, robots, map, css;

		index = SmartTagParser.read(EmbeddedFiles.class
				.getResourceAsStream("/index.html"));

		ajax = SmartTagParser.read(EmbeddedFiles.class
				.getResourceAsStream("/ajax.js"));

		robots = SmartTagParser.read(EmbeddedFiles.class
				.getResourceAsStream("/robots.txt"));

		map = SmartTagParser.read(EmbeddedFiles.class
				.getResourceAsStream("/map.txt"));

		css = SmartTagParser.read(EmbeddedFiles.class
				.getResourceAsStream("/screen.css"));

		this.pages.put("/", new GzipResponse(index, ".html"));
		this.pages.put("/ajax.js", new GzipResponse(ajax, ".js"));

		this.pages.put("/robots.txt", new GzipResponse(robots, ".txt"));

		this.pages.put("/sitemap.xml", new GzipResponse(map, ".xml"));

		this.pages.put("/screen.css", new GzipResponse(css, ".css"));

		if (!config.GOOGLE_VERIFICATION.isEmpty()) {
			this.pages
					.put(config.GOOGLE_VERIFICATION,
							new GzipResponse(
									("/google-site-verification: " + config.GOOGLE_VERIFICATION)
											.getBytes(), ".html"));
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

	@Override
	public List<String> browse() {
		List<String> result = new ArrayList<>(pages.size());
		result.addAll(pages.keySet());
		return result;
	}

}
