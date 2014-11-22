package pt.go2.fileio;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.response.AbstractResponse;
import pt.go2.response.GzipResponse;

public class LocalFiles implements FileSystemInterface, Runnable {

	static final Logger logger = LogManager.getLogger(FileSystemInterface.class);

	private final Map<String, AbstractResponse> pages = new ConcurrentHashMap<>();

	private final Configuration conf;

	private final int trim;

	private volatile boolean running;
	private final WatchService watchService;

	public LocalFiles(Configuration conf) throws IOException {

		this.conf = conf;

		this.trim = conf.getPublicRoot().length() + 1;

		this.watchService = FileSystems.getDefault().newWatchService();

		final List<Path> files = new ArrayList<>();
		final List<Path> directories = new ArrayList<>();

		FileCrawler.crawl(conf.getPublicRoot(), directories, files);

		for (Path path : files) {

			addStaticPage(path);
		}

		for (Path path : directories) {
			try {
				path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY,
						StandardWatchEventKinds.ENTRY_DELETE);
			} catch (IOException e) {
				logger.warn("Could not registed directory: " + path.toString());
			}
		}

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public AbstractResponse getFile(String filename) {
		return pages.get(filename);
	}

	@Override
	public void run() {

		running = true;
		while (watchService != null && running) {

			final WatchKey key = watchService.poll();

			if (key == null) {
				continue;
			}

			// get list of pending events for the watch key
			for (WatchEvent<?> watchEvent : key.pollEvents()) {

				final Kind<?> kind = watchEvent.kind();

				if (kind == StandardWatchEventKinds.OVERFLOW) {
					continue;
				}

				// get the filename for the event
				@SuppressWarnings("unchecked")
				final WatchEvent<Path> watchEventPath = (WatchEvent<Path>) watchEvent;
				final Path filename = watchEventPath.context();

				if (kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
					addStaticPage(filename);
				}

				if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
					this.pages.remove(filename);
				}
			}

			key.reset();
		}
	}

	private void addStaticPage(final Path path) {

		final String filename = path.toString();

		final int idx = filename.lastIndexOf('.');

		final String mimeType;

		if (idx == -1) {
			mimeType = AbstractResponse.MIME_TEXT_PLAIN;
		} else {

			final String extension = filename.substring(idx);

			switch (extension) {
			case ".css":
				mimeType = AbstractResponse.MIME_TEXT_CSS;
				break;
			case ".gif":
				mimeType = AbstractResponse.MIME_IMG_GIF;
				break;
			case ".html":
			case ".htm":
				mimeType = AbstractResponse.MIME_TEXT_HTML;
				break;
			case ".jpeg":
			case ".jpg":
				mimeType = AbstractResponse.MIME_IMG_JPEG;
				break;
			case ".js":
				mimeType = AbstractResponse.MIME_APP_JAVASCRIPT;
				break;
			case ".png":
				mimeType = AbstractResponse.MIME_IMG_PNG;
				break;
			case ".xml":
				mimeType = AbstractResponse.MIME_TEXT_XML;
				break;
			default:
				mimeType = AbstractResponse.MIME_TEXT_PLAIN;
				break;
			}
		}

		try {

			this.pages.put(filename.substring(trim), new GzipResponse(SmartTagParser.read(filename, conf), mimeType));

		} catch (IOException e) {

			logger.error("Failed loading: " + filename);
		}
	}

}
