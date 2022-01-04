package pt.go2.urlshortener.repositories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

@Repository
@Slf4j
public class ShortUrlRepository {

	private final Map<String, ShortUrl> keyToShortUrl = new HashMap<>();
	private final Map<String, ShortUrl> urlToShortUrl = new HashMap<>();

	private ByteBuffer buildBuffer(String content) {
		final var bytes = content.getBytes();
		final var buffer = ByteBuffer.allocate(bytes.length);
		buffer.put(bytes);
		buffer.flip();
		return buffer;
	}

	public ShortUrl findByKey(String key) {
		return keyToShortUrl.get(key);
	}

	public ShortUrl findByUrl(String url) {
		return urlToShortUrl.get(url);
	}

	public ShortUrl create(ShortUrl url) {
		keyToShortUrl.put(url.getKey(), url);
		urlToShortUrl.put(url.getUrl(), url);
		return url;
	}
}
