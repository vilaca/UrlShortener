package pt.go2.urlshortener.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import pt.go2.urlshortener.exceptions.ClientException;
import pt.go2.urlshortener.exceptions.NotFoundException;
import pt.go2.urlshortener.io.Resume;
import pt.go2.urlshortener.io.ShortUrl;
import pt.go2.urlshortener.io.Storage;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedirectTest {

  @Autowired
  private ShortenerController controller;

  @Value("${server.redirect}")
  private int redirectStatus;

  @Autowired
  private Storage storage;

  @Before
  public void clear() {
    this.storage.clear();

    final Resume restore = Mockito.mock(Resume.class);
    when(restore.load()).thenReturn(Collections.emptyList());
  }

  @Test
  public void findAll() {

    final String header = "Location";
    final String hash1 = "123456";
    final String hash2 = "654321";
    final String hash3 = "XXXXXX";

    final String url1 = "http://vilaca.eu";
    final String url2 = "http://go2.pt";
    final String url3 = "https://go2.pt";

    this.storage.store(new ShortUrl(hash1), url1);
    this.storage.store(new ShortUrl(hash2), url2);
    this.storage.store(new ShortUrl(hash3), url3);

    final ResponseEntity<?> r1 = this.controller.redirect(hash1);
    final int s1 = r1.getStatusCodeValue();
    final List<String> l1 = r1.getHeaders().get(header);

    final ResponseEntity<?> r2 = this.controller.redirect(hash2);
    final int s2 = r2.getStatusCodeValue();
    final List<String> l2 = r2.getHeaders().get(header);

    final ResponseEntity<?> r3 = this.controller.redirect(hash3);
    final int s3 = r3.getStatusCodeValue();
    final List<String> l3 = r3.getHeaders().get(header);

    assertEquals(this.redirectStatus, s1);
    assertEquals(1, l1.size());
    assertEquals(url1, l1.get(0));

    assertEquals(this.redirectStatus, s2);
    assertEquals(1, l2.size());
    assertEquals(url2, l2.get(0));

    assertEquals(this.redirectStatus, s3);
    assertEquals(1, l3.size());
    assertEquals(url3, l3.get(0));
  }

  @Test(expected = NotFoundException.class)
  public void notFound() {

    final String key = "Location";

    final String hash1 = "123456";
    final String hash2 = "654321";
    final String hash3 = "XXXXXX";

    final String url1 = "http://vilaca.eu";
    final String url2 = "http://go2.pt";

    this.storage.store(new ShortUrl(hash1), url1);
    this.storage.store(new ShortUrl(hash2), url2);

    final ResponseEntity<?> r1 = this.controller.redirect(hash1);
    final int s1 = r1.getStatusCodeValue();
    final List<String> l1 = r1.getHeaders().get(key);

    final ResponseEntity<?> r2 = this.controller.redirect(hash2);
    final int s2 = r2.getStatusCodeValue();
    final List<String> l2 = r2.getHeaders().get(key);

    assertEquals(this.redirectStatus, s1);
    assertEquals(1, l1.size());
    assertEquals(url1, l1.get(0));

    assertEquals(this.redirectStatus, s2);
    assertEquals(1, l2.size());
    assertEquals(url2, l2.get(0));

    this.controller.redirect(hash3);
  }

  @Test(expected = ClientException.class)
  public void shortUrlTooShort() {
    this.controller.redirect("12");
  }

  @Test(expected = ClientException.class)
  public void shortUrlTooLong() {
    this.controller.redirect("122345678");
  }

  @Test(expected = ClientException.class)
  public void shortUrlTooBlank() {
    this.controller.redirect("    ");
  }
}
