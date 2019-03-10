/*
 * MIT License
 *
 * Copyright (c) 2019 João Vilaça
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package pt.go2.urlshortener.rest;

import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.go2.urlshortener.exceptions.ClientException;
import pt.go2.urlshortener.exceptions.NotFoundException;
import pt.go2.urlshortener.exceptions.ServerException;
import pt.go2.urlshortener.io.ShortUrl;

@RestController
public class ShortenerController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ShortenerController.class);

  private final int redirectStatus;

  private final ShortenerService service;

  public ShortenerController(ShortenerService service, @Value("${server.redirect}") int redirectStatus) {
    this.service = service;
    this.redirectStatus = redirectStatus;
  }

  @PostMapping(path = "/")
  public ResponseEntity<Response> newUrl(@RequestParam(value = "v") String url) {

    if (!new UrlValidator(new String[] { "http", "https" }).isValid(url)) {
      throw new ClientException("Invalid Url.");
    }

    final String shortUrl = this.service.newUrl(url);

    if (shortUrl == null) {
      LOGGER.error("Failed to create short url: {}.", url);
      throw new ServerException();
    }

    return new ResponseEntity<>(Response.ok(shortUrl), HttpStatus.CREATED);
  }

  @GetMapping(path = "/{hash}")
  public ResponseEntity<Response> redirect(@PathVariable(value = "hash") String hash) {

    if (hash == null || hash.trim().isEmpty()) {
      throw new ClientException("Expected short url.");
    }

    if (hash.trim().length() != ShortUrl.SHORT_URL_LEN) {
      throw new ClientException("Short url not valid.");
    }

    final String url = this.service.getRedirect(hash);
    if (url == null) {
      throw new NotFoundException();
    }

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Location", url);
    return new ResponseEntity<>(Response.ok(), headers,
        this.redirectStatus == HttpStatus.FOUND.value() ? HttpStatus.FOUND : HttpStatus.MOVED_PERMANENTLY);
  }
}
