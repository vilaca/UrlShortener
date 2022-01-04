package pt.go2.urlshortener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import pt.go2.urlshortener.api.Controller;
import pt.go2.urlshortener.model.NewUrl;
import pt.go2.urlshortener.model.SavedShortUrl;
import pt.go2.urlshortener.repositories.ShortUrl;
import pt.go2.urlshortener.services.UrlShortenerService;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@DirtiesContext
public class ControllerTest {

	private WebTestClient webTestClient;

	@MockBean
	private UrlShortenerService mockService;

	@InjectMocks
	private Controller controller;

	@LocalServerPort
	private int randomServerPort;

	@BeforeEach
	public void setup() {
		this.webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + randomServerPort).build();
	}

	@Test
	void GIVEN_valid_url_WHEN_calling_put_THEN_return_ok_and_ShortUrl_in_response() {

		final var shortUrl = new SavedShortUrl().setUrl("http://localhost/").setKey("key");

		when(mockService.put(any())).thenReturn(Mono.just(shortUrl));

		webTestClient.post()
				.uri(UrlShortener.BASE_PATH)
				.bodyValue(shortUrl)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().exists("Location")
				.expectBody(ShortUrl.class)
				.value(device -> assertNotNull(device.getUrl()))
				.value(device -> assertNotNull(device.getKey()));

		verify(mockService, times(1)).put(any());
	}

	@Test
	void GIVEN_url_too_short_WHEN_calling_create_THEN_return_BAD_REQUEST() {

		final var shortUrl = new NewUrl().setUrl("12345");
		webTestClient.post()
				.uri(UrlShortener.BASE_PATH)
				.bodyValue(shortUrl)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void GIVEN_url_too_long_WHEN_calling_create_THEN_return_BAD_REQUEST() {

		final var shortUrl = new NewUrl().setUrl("http://localhost/" + "!".repeat(2048));
		webTestClient.post()
				.uri(UrlShortener.BASE_PATH)
				.bodyValue(shortUrl)
				.exchange()
				.expectStatus().isBadRequest();
	}


	@Test
	void GIVEN_empty_url_WHEN_calling_create_THEN_return_BAD_REQUEST() {

		final var shortUrl = new NewUrl().setUrl("");
		webTestClient.post()
				.uri(UrlShortener.BASE_PATH)
				.bodyValue(shortUrl)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void GIVEN_null_url_WHEN_calling_create_THEN_return_BAD_REQUEST() {

		final var shortUrl = new NewUrl().setUrl(null);
		webTestClient.post()
				.uri(UrlShortener.BASE_PATH)
				.bodyValue(shortUrl)
				.exchange()
				.expectStatus().isBadRequest();
	}
}
