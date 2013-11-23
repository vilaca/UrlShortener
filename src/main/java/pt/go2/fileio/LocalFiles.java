package pt.go2.fileio;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.response.AbstractResponse;
import pt.go2.response.GzipResponse;
import pt.go2.response.RedirectResponse;

public class LocalFiles implements FileSystemInterface, Runnable {

	static final Logger logger = LogManager
			.getLogger(FileSystemInterface.class);

	private final Map<String, AbstractResponse> pages = new ConcurrentHashMap<>();
	private final Map<WatchKey, String> keys = new HashMap<>();
	private final Set<String> directories = new HashSet<>();

	private final int trim;

	private volatile boolean running;

	private final WatchService watchService;

	private final Configuration config;

	public LocalFiles(Configuration config) throws IOException {

		this.config = config;

		this.trim = config.PUBLIC.length() + 1;

		this.watchService = FileSystems.getDefault().newWatchService();

		final List<Path> files = new ArrayList<>();
		final Set<Path> directories = new HashSet<>();

		FileCrawler.crawl(config.PUBLIC, directories, files);

		for (Path path : files) {

			addStaticPage(path);
		}

		for (Path path : directories) {
			try {
				register(path);
				this.directories.add(path.toString());
			} catch (IOException e) {
				logger.warn("Could not registed directory: " + path.toString());
			}
		}
	}

	private void register(Path path) throws IOException {
		final WatchKey key = path.register(watchService,
				StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_MODIFY,
				StandardWatchEventKinds.ENTRY_DELETE);

		keys.put(key, path.toString());
	}

	@Override
	public void start() {
		new Thread(this).start();
	}

	@Override
	public void stop() {
		running = false;
	}

	@Override
	public AbstractResponse getFile(String filename) {

		if (filename.endsWith("/") && config.PUBLIC != null) {
			filename += config.PUBLIC_ROOT;
		}

		filename = filename.replace('/', File.separatorChar);

		final AbstractResponse page = pages.get(filename);

		if (page != null) {
			return page;
		}

		if (!filename.endsWith("/")) {

			final String directory = config.PUBLIC + File.separator + filename;

			if (directories.contains(directory)) {
				return new RedirectResponse("/" + filename + "/", 301);
			}
		}

		return null;
	}

	@Override
	public void run() {

		running = true;

		while (watchService != null && running) {

			WatchKey key;
			try {
				key = watchService.poll(5, TimeUnit.SECONDS);
			} catch (InterruptedException e1) {
				continue;
			}

			if (key == null) {
				continue;
			}

			for (WatchEvent<?> watchEvent : key.pollEvents()) {

				final Kind<?> kind = watchEvent.kind();

				if (kind == StandardWatchEventKinds.OVERFLOW) {
					continue;
				}

				// get the filename for the event
				@SuppressWarnings("unchecked")
				final WatchEvent<Path> watchEventPath = (WatchEvent<Path>) watchEvent;

				final Path path = watchEventPath.context();
				final String child = path.getFileName().toString();

				final String parent = keys.get(key);

				final String filename = parent + File.separator + child;

				if (kind == StandardWatchEventKinds.ENTRY_CREATE) {

					if (new File(filename).isDirectory()) {
						try {
							register(path);
						} catch (IOException e) {
						}
					} else {
						addStaticPage(filename);
					}
					continue;
				}

				if (pages.containsKey(child)) {
					// file action

					if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
						pages.remove(child);
					}
					continue;
				}

			}

			if (!key.reset()) {
				keys.remove(key);
				key.cancel();
			}
		}
	}

	private void addStaticPage(final Path path) {

		addStaticPage(path.toString());
	}

	private void addStaticPage(final String filename) {

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

			this.pages.put(filename.substring(trim), new GzipResponse(
					SmartTagParser.read(filename), mimeType));

		} catch (IOException e) {

			logger.error("Failed loading: " + filename);
		}
	}

}
