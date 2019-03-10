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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import pt.go2.urlshortener.filters.LoggingFilter;
import pt.go2.urlshortener.io.ShortUrl;

@Service
class ShortenerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);

  private final ShortenerRepository repository;

  public ShortenerService(ShortenerRepository repository) {
    this.repository = repository;
  }

  public String getRedirect(String hash) {
    return this.repository.findHash(new ShortUrl(hash));
  }

  public String newUrl(String url) {

    if (!url.startsWith("http://") && !url.startsWith("https://")) {
      url = "http://" + url;
    }

    final ShortUrl stored = this.repository.findUrl(url);
    if (stored != null) {
      LOGGER.info("Short url {} for {} already existed.", stored, url);
      return stored.toString();
    }

    ShortUrl shortUrl = new ShortUrl();
    while (!this.repository.store(shortUrl, url)) {
      LOGGER.error("Short url (hash) already in use {}.", shortUrl);
      shortUrl = new ShortUrl();
    }

    LOGGER.info("New short url: {} for {}.", shortUrl, url);
    try {
      this.repository.write(shortUrl, url);
      return shortUrl.toString();
    } catch (final IOException e) {
      LOGGER.error("Couldn't save short URL {} : {}.", shortUrl, url, e);
      return null;
    }
  }
}
