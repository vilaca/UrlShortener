package pt.go2.urlshortener;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Url Shortener", version = "1.0", description = "Url Shortener APIs v1.0"))
public class UrlShortener {

	public static final String BASE_PATH = "/shortener";

	public static void main(String[] args) {
		SpringApplication.run(UrlShortener.class, args);
	}
}
