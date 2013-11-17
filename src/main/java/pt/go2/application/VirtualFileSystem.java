package pt.go2.application;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.daemon.PhishTankInterface;
import pt.go2.daemon.WatchDog;
import pt.go2.fileio.Configuration;
import pt.go2.fileio.FileCrawler;
import pt.go2.fileio.SmartTagParser;
import pt.go2.keystore.HashKey;
import pt.go2.keystore.KeyValueStore;
import pt.go2.keystore.Uri;
import pt.go2.response.AbstractResponse;
import pt.go2.response.ErrorResponse;
import pt.go2.response.GzipResponse;
import pt.go2.response.RedirectResponse;

/**
 * Virtualize file resources
 * 
 */
public class VirtualFileSystem {

	/**
	 * Canned responses for errors
	 */

	enum Error {
		PAGE_NOT_FOUND, REJECT_SUBDOMAIN, BAD_REQUEST, FORBIDDEN_PHISHING
	}

	static final Logger logger = LogManager.getLogger(VirtualFileSystem.class);

	private WatchDog watchdog = new WatchDog();

	// error responses
	private final Map<Error, AbstractResponse> errors = new EnumMap<>(
			Error.class);

	// responses
	private Map<String, AbstractResponse> pages;

	// key = hash / value = uri
	private KeyValueStore ks;

	private PhishTankInterface pi;

	private volatile boolean running;
	private WatchService watchService;

	/**
	 * C'tor
	 * 
	 * @param ks
	 */
	public VirtualFileSystem() {
	}

	public boolean start(final Configuration config) {

		try {
			this.ks = new KeyValueStore(config.DATABASE_FOLDER);
		} catch (IOException e) {
			logger.fatal("Could not read backup.");
			return false;
		}

		final SmartTagParser fr = new SmartTagParser("/");

		if (!createErrorPages(config, fr)) {
			logger.fatal("Could not create error pages.");
			return false;
		}

		if (config.STATISTICS_FOLDER.isEmpty()) {

			this.pages = new HashMap<>();

			if (!createEmbeddedPages(config, fr)) {
				logger.fatal("Could not create embedded pages.");
				return false;
			}

			this.watchService = null;
		} else {

			try {
				this.watchService = FileSystems.getDefault().newWatchService();
			} catch (IOException e1) {
				logger.fatal("Could not load public pages.");
				return false;
			}

			this.pages = new ConcurrentHashMap<>();

			final List<Path> files = new ArrayList<>();
			final List<Path> directories = new ArrayList<>();

			try {
				FileCrawler.crawl(config.STATISTICS_FOLDER, directories, files);
			} catch (IOException e) {

				logger.fatal("Could not load static pages.");
				return false;
			}

			for (Path path : files) {

				addStaticPage(fr, path);
			}

			for (Path path : directories) {
				try {
					path.register(watchService,
							StandardWatchEventKinds.ENTRY_CREATE,
							StandardWatchEventKinds.ENTRY_MODIFY,
							StandardWatchEventKinds.ENTRY_DELETE);
				} catch (IOException e) {
					logger.warn("Could not registed directory: "
							+ path.toString());
				}
			}
		}

		this.pi = PhishTankInterface.create(config);

		if (pi == null) {
			logger.warn("Could init PhishTank API Interface.");
		} else {
			watchdog.register(pi, true);
		}
		return true;
	}

	private void addStaticPage(final SmartTagParser fr, final Path path) {

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

			this.pages.put(filename, new GzipResponse(fr.read(filename),
					mimeType));

		} catch (IOException e) {

			logger.error("Failed loading: " + filename);
		}
	}

	private void listen() {

		running = true;
		while (running) {

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

				if (kind == StandardWatchEventKinds.ENTRY_CREATE
						|| kind == StandardWatchEventKinds.ENTRY_MODIFY) {
					addStaticPage(new SmartTagParser(""), filename);
				}

				if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
					this.pages.remove(filename);
				}
			}

			key.reset();
		}
	}

	/**
	 * Return error response
	 * 
	 * @param badRequest
	 * @return
	 */
	public AbstractResponse get(Error error) {
		return errors.get(error);
	}

	/**
	 * Read pages from JAR
	 * 
	 * @param config
	 * @param fr
	 * @return
	 */
	private boolean createEmbeddedPages(final Configuration config,
			final SmartTagParser fr) {

		final byte[] index, ajax, robots, map, css;

		try {
			index = fr.read("index.html");
			ajax = fr.read("ajax.js");
			robots = fr.read("robots.txt");
			map = fr.read("map.txt");
			css = fr.read("screen.css");
		} catch (IOException e) {
			logger.fatal("Can't read embedded page.");
			return false;
		}

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

		return true;
	}

	/**
	 * Cache error responses
	 * 
	 * @param config
	 * @param fr
	 * @return
	 */
	private boolean createErrorPages(final Configuration config,
			final SmartTagParser fr) {

		try {
			this.errors.put(Error.PAGE_NOT_FOUND,
					new ErrorResponse(fr.read("404.html"), 404,
							AbstractResponse.MIME_TEXT_HTML));
		} catch (IOException e) {
			logger.fatal("Cannot read 404 page.");
			return false;
		}

		// TODO are you sure this is a good message ??
		try {
			this.errors.put(Error.FORBIDDEN_PHISHING,
					new ErrorResponse(fr.read("403.html"), 403,
							AbstractResponse.MIME_TEXT_HTML));
		} catch (IOException e) {
			logger.fatal("Cannot read 403 page.");
			return false;
		}

		this.errors.put(Error.BAD_REQUEST,
				new ErrorResponse("Bad request.".getBytes(), 400,
						AbstractResponse.MIME_TEXT_PLAIN));

		// redirect to domain if a sub-domain is being used

		this.errors.put(Error.REJECT_SUBDOMAIN, new RedirectResponse("http://"
				+ config.ENFORCE_DOMAIN, 301));

		return true;
	}

	public boolean isBanned(Uri uri) {

		if (uri.getState() != Uri.State.OK) {
			return true;
		}

		return pi.isBanned(uri);
	}

	public byte[] add(Uri uri) {
		return ks.add(uri);
	}

	public Uri get(HashKey haskey) {
		return ks.get(haskey);
	}

	public AbstractResponse get(String requested) {
		return pages.get(requested);
	}
}
