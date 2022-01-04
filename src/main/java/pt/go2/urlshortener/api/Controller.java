package pt.go2.urlshortener.api;


import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.go2.urlshortener.UrlShortener;
import pt.go2.urlshortener.model.NewUrl;
import pt.go2.urlshortener.model.SavedShortUrl;
import pt.go2.urlshortener.services.UrlShortenerService;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.net.URI;

@RestController
@RequestMapping(value = UrlShortener.BASE_PATH)
@RequiredArgsConstructor
@Slf4j
public class Controller {

	private final UrlShortenerService service;

	@ApiResponse(responseCode = "201", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SavedShortUrl.class)))
	@ApiResponse(responseCode = "400", content = @Content(mediaType = "application/json", schema = @Schema()))
	@ApiResponse(responseCode = "500", content = @Content(mediaType = "application/json", schema = @Schema()))
	@PostMapping
	public Mono<ResponseEntity<SavedShortUrl>> create(@Valid @RequestBody Mono<NewUrl> body) {
		return body.flatMap(shortUrl -> service.put(shortUrl)
				.flatMap(s -> Mono.just(ResponseEntity.created(URI.create(UrlShortener.BASE_PATH + "/" + s.getKey())).body(s))));
	}

	@ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SavedShortUrl.class)))
	@ApiResponse(responseCode = "400", content = @Content(mediaType = "application/json", schema = @Schema()))
	@ApiResponse(responseCode = "404", content = @Content(mediaType = "application/json", schema = @Schema()))
	@ApiResponse(responseCode = "500", content = @Content(mediaType = "application/json", schema = @Schema()))
	@GetMapping("/{key}")
	public Mono<SavedShortUrl> listById(@NotBlank @PathVariable("key") String key) {
		return service.getByKey(key);
	}
}