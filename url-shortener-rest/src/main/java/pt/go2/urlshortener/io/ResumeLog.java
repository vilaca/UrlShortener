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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ResumeLog {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResumeLog.class);

  private final BufferedWriter writer;

  public ResumeLog(@Value("${database.folder}") String resumeFolder) throws IOException {

    String filename;
    do {
      filename = generateFilename(resumeFolder);
    } while (new File(filename).exists());

    LOGGER.info("Appending to {} in folder {}.", filename, resumeFolder);
    final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8);
    this.writer = new BufferedWriter(osw);
  }

  @PreDestroy
  public void bye() {
    try {
      this.writer.flush();
      this.writer.close();
    } catch (final IOException e) {
      LOGGER.info("Error shutting down.", e);
    }
  }

  public synchronized void write(ShortUrl hash, String url) throws IOException {
    this.writer.write(String.format("%s,%s%n", hash.toString(), url));
    this.writer.flush();
  }

  private static String generateFilename(final String resumeFolder) {
    return resumeFolder + String.valueOf(Instant.now().toEpochMilli());
  }
}
