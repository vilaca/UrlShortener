package pt.go2.fileio;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileCrawler {

	static final Logger logger = LogManager.getLogger(FileCrawler.class);

	public static void crawl(final String start, final Set<Path> directories,
			final List<Path> files) throws IOException {

		Files.walkFileTree(Paths.get(start), new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(final Path dir,
					final BasicFileAttributes attrs) {

				logger.info("Indexing dir: " + dir.getFileName());

				directories.add(dir);

				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(final Path file,
					final BasicFileAttributes attrs) {

				logger.info("Indexing file: " + file.getFileName());

				files.add(file);

				return FileVisitResult.CONTINUE;
			}
		});
	}

	private FileCrawler() {
	}
}
