package pt.go2.urlshortener;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UrlShortenerTest {
	@Test
	void main_SHOULD_loadContext_WHEN_applicationstarts() {
		assertNotNull(this);
	}
}
