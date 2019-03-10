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

import org.springframework.stereotype.Repository;

import pt.go2.urlshortener.io.ResumeLog;
import pt.go2.urlshortener.io.ShortUrl;
import pt.go2.urlshortener.io.Storage;

@Repository
class ShortenerRepository {

  private final ResumeLog backup;
  private final Storage storage;

  public ShortenerRepository(ResumeLog backup, Storage storage) {
    this.storage = storage;
    this.backup = backup;
  }

  public String findHash(ShortUrl hash) {
    return this.storage.findHash(hash);
  }

  public ShortUrl findUrl(String url) {
    return this.storage.findUrl(url);
  }

  public boolean store(ShortUrl hash, String url) {
    return this.storage.store(hash, url);
  }

  public void write(ShortUrl hash, String url) throws IOException {
    this.backup.write(hash, url);
  }
}
