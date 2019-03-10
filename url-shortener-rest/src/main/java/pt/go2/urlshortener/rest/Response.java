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

class Response {

  public enum Success {
    YES, NO
  }

  private final Success success;

  private final String data;

  private Response(Success success, String result) {
    this.success = success;
    this.data = result;
  }

  public static Response fail(String message) {
    return new Response(Success.NO, message);
  }

  public static Response ok() {
    return new Response(Success.NO, null);
  }

  public static Response ok(String result) {
    return new Response(Success.NO, result);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final Response other = (Response) obj;
    if (this.data == null) {
      if (other.data != null) {
        return false;
      }
    } else {
      if (!this.data.equals(other.data)) {
        return false;
      }
    }
    return this.success == other.success;
  }

  public String getResult() {
    return this.data;
  }

  public Success getSuccess() {
    return this.success;
  }

  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + (this.data == null ? 0 : this.data.hashCode());
    result = PRIME * result + (this.success == null ? 0 : this.success.hashCode());
    return result;
  }
}
