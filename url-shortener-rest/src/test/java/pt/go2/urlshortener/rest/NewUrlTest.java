package pt.go2.urlshortener.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import pt.go2.urlshortener.io.Resume;
import pt.go2.urlshortener.io.ResumeLog;
import pt.go2.urlshortener.io.ShortUrl;
import pt.go2.urlshortener.io.Storage;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NewUrlTest {

  private static final int CREATED_HTTP_STATUS = HttpStatus.CREATED.value();

  @Autowired
  private ShortenerController controller;

  @Autowired
  private Storage storage;

  private final Resume restore = Mockito.mock(Resume.class);
  private final ResumeLog backup = Mockito.mock(ResumeLog.class);

  @Before
  public void clear() throws IOException {
    this.storage.clear();
    when(this.restore.load()).thenReturn(Collections.emptyList());
    doNothing().when(this.backup).write(isA(ShortUrl.class), isA(String.class));
  }

  @Test
  public void reuseAndUniqueAll() {

    final String url1 = "http://vilaca.eu";
    final String url2 = "http://go2.pt";
    final String url3 = "https://go2.pt";

    final ResponseEntity<Response> r1r1 = this.controller.newUrl(url1);
    final ResponseEntity<Response> r1r2 = this.controller.newUrl(url1);
    final ResponseEntity<Response> r2r1 = this.controller.newUrl(url2);
    final ResponseEntity<Response> r2r2 = this.controller.newUrl(url2);
    final ResponseEntity<Response> r3r1 = this.controller.newUrl(url3);
    final ResponseEntity<Response> r1r3 = this.controller.newUrl(url1);

    assertEquals(CREATED_HTTP_STATUS, r1r1.getStatusCodeValue());
    assertEquals(CREATED_HTTP_STATUS, r1r2.getStatusCodeValue());
    assertEquals(CREATED_HTTP_STATUS, r1r3.getStatusCodeValue());
    assertEquals(CREATED_HTTP_STATUS, r2r1.getStatusCodeValue());
    assertEquals(CREATED_HTTP_STATUS, r2r2.getStatusCodeValue());
    assertEquals(CREATED_HTTP_STATUS, r3r1.getStatusCodeValue());

    assertEquals(r1r1, r1r2);
    assertEquals(r1r1, r1r3);
    assertEquals(r2r1, r2r2);

    assertNotEquals(r1r1, r2r1);
    assertNotEquals(r1r1, r3r1);
    assertNotEquals(r2r1, r3r1);
  }
}
