package pt.go2.urlshortener.repositories;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@NoArgsConstructor
@Data
@Accessors(chain = true)
public class ShortUrl {
	private String key;
	private String url;
}
