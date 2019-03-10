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
package pt.go2.urlshortener.io;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class Storage {

  private final Map<ShortUrl, String> hash2url = new HashMap<>();
  private final Map<String, ShortUrl> url2hash = new HashMap<>();

  private final Resume resume;

  public Storage(Resume resume) {
    this.resume = resume;
  }

  public synchronized void clear() {
    this.url2hash.clear();
    this.hash2url.clear();
  }

  public synchronized String findHash(ShortUrl hash) {
    return this.hash2url.get(hash);
  }

  public synchronized ShortUrl findUrl(String url) {
    return this.url2hash.get(url);
  }

  @PostConstruct
  public void init() {
    for (final ResumeItem ri : this.resume.load()) {
      final ShortUrl su = new ShortUrl(ri.getKey());
      this.hash2url.putIfAbsent(su, ri.getValue());
      this.url2hash.put(ri.getKey(), su);
    }
  }

  public synchronized boolean store(ShortUrl hash, String url) {
    if (this.hash2url.putIfAbsent(hash, url) != null) {
      return false;
    }
    this.url2hash.put(url, hash);
    return true;
  }
}
