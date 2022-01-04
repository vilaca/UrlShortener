package pt.go2.urlshortener;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pt.go2.urlshortener.repositories.ShortUrl;
import pt.go2.urlshortener.repositories.ShortUrlRepository;
import pt.go2.urlshortener.services.UrlShortenerService;

import static org.mockito.Mockito.*;

public class UrlShortenerServiceTest {

	private ShortUrlRepository mockRepository = Mockito.mock(ShortUrlRepository.class);

	private UrlShortenerService service = new UrlShortenerService(mockRepository);

	@Test
	void GIVEN_empty_brand_parameter_WHEN_list_THEN_findAll_is_called() {
		when(mockRepository.create(any())).thenReturn(new ShortUrl());
		service.list("");
		verify(mockRepository, times(1)).findAll();
	}


//	@Test
//	void GIVEN_empty_brand_parameter_WHEN_list_THEN_findAll_is_called() {
//		when(mockRepository.findAll()).thenReturn(Flux.empty());
//		service.list("");
//		verify(mockRepository, times(1)).findAll();
//	}
//
//	@Test
//	void GIVEN_not_empty_brand_parameter_WHEN_list_THEN_findAll_is_called() {
//		when(mockRepository.findByBrand(anyString())).thenReturn(Flux.empty());
//		service.list("brand-name");
//		verify(mockRepository, times(1)).findByBrand(anyString());
//	}
}
