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

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Short Url. Composed of 6 chars BASE64 encoded.
 *
 * @author vilaca
 */
public class ShortUrl {

  private static final List<Character> BASE64_CHARS = Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
      'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
      'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
      '4', '5', '6', '7', '8', '9', '+', '_');

  public static final int SHORT_URL_LEN = 6;

  private final byte[] hash;

  public ShortUrl() {
    this(random());
  }

  public ShortUrl(byte[] hash) {
    this.hash = Arrays.copyOf(hash, hash.length);
  }

  public ShortUrl(String hash) {
    this(hash(hash));
  }

  private static char byte2char(byte b) {
    return BASE64_CHARS.get(b);
  }

  private static byte char2byte(char charAt) {
    return (byte) BASE64_CHARS.indexOf(charAt);
  }

  private static byte[] hash(String hash) {
    final byte[] res = new byte[SHORT_URL_LEN];
    for (int i = 0; i < SHORT_URL_LEN; i++) {
      res[i] = char2byte(hash.charAt(i));
    }
    return res;
  }

  /**
   * Random short url.
   *
   * @return
   */
  private static byte[] random() {
    final Random rand = ThreadLocalRandom.current();
    return new byte[] {
        // 1st random char
        (byte) rand.nextInt(BASE64_CHARS.size()),
        // 2nd random char
        (byte) rand.nextInt(BASE64_CHARS.size()),
        // 3rd random char
        (byte) rand.nextInt(BASE64_CHARS.size()),
        // 4th random char
        (byte) rand.nextInt(BASE64_CHARS.size()),
        // 5th random char
        (byte) rand.nextInt(BASE64_CHARS.size()),
        // 6th random char
        (byte) rand.nextInt(BASE64_CHARS.size()) };
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o != null && o.getClass() == this.getClass()) {
      return Arrays.equals(((ShortUrl) o).hash, this.hash);
    }
    return false;
  }

  /*
   * hashCode() implementation.
   *
   * A ShortUrl instance needs 36bits, hashCode() only uses 32b.
   *
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = this.hash[0] & 3;
    hashCode = hashCode * 64 + this.hash[1];
    hashCode = hashCode * 64 + this.hash[2];
    hashCode = hashCode * 64 + this.hash[3];
    hashCode = hashCode * 64 + this.hash[4];
    hashCode = hashCode * 64 + this.hash[5];
    return hashCode;
  }

  /*
   * Human readable form.
   *
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    int idx = 0;
    sb.append(byte2char(this.hash[idx++]));
    sb.append(byte2char(this.hash[idx++]));
    sb.append(byte2char(this.hash[idx++]));
    sb.append(byte2char(this.hash[idx++]));
    sb.append(byte2char(this.hash[idx++]));
    sb.append(byte2char(this.hash[idx]));
    return sb.toString();
  }
}
