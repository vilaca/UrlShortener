package pt.go2.urlshortener.services;

import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pt.go2.urlshortener.model.NewUrl;
import pt.go2.urlshortener.model.SavedShortUrl;
import pt.go2.urlshortener.repositories.ShortUrl;
import pt.go2.urlshortener.repositories.ShortUrlRepository;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class UrlShortenerService {

	private static final byte[] VALID_CHARS = "ABCEDFGHIJKLMNOPQRSTUVWXYZabcedefghijklmnopqrstuvwxyz0123456789-_".getBytes();
	private final ShortUrlRepository repository;

	public Mono<SavedShortUrl> put(NewUrl url) {
		if (!new UrlValidator().isValid(url.getUrl())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Url.");
		}
		final var found = repository.findByUrl(url.getUrl());
		if (found != null) {
			return Mono.just(new SavedShortUrl().setUrl(found.getUrl()).setKey(found.getKey()));
		}
		final var shortUrl = new ShortUrl().setUrl(url.getUrl());
		final var saved = repository.create(shortUrl.setKey(createUniqueKey()));
		return Mono.just(new SavedShortUrl().setUrl(saved.getUrl()).setKey(saved.getKey()));
	}

	private String createUniqueKey() {
		String key;
		do {
			final var newKey = new byte[6];
			final var rnd = new Random();
			for (int i = 0; i < 6; i++) {
				newKey[i] = VALID_CHARS[rnd.nextInt() % VALID_CHARS.length];
			}
			key = new String(newKey, StandardCharsets.UTF_8);
		} while (repository.findByKey(key) != null);
		return key;
	}

	public Mono<SavedShortUrl> getByKey(String key) {
		return null;
	}
}

