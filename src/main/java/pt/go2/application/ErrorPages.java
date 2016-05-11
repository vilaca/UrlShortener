/*
    Copyright (C) 2016 João Vilaça

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
*/
package pt.go2.application;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

/**
 * Responses used to report and error has occurred.
 * 
 * @author vilaca
 */
enum ErrorPages implements Response {

    PAGE_NOT_FOUND(Response.readFile("/404.html"), HttpStatus.NOT_FOUND_404, MimeTypeConstants.MIME_TEXT_HTML),
    
    // this error should be returned when an URL was detected as being a
    // phishing site **after** it was added to the database
    PHISHING(Response.readFile("/403-phishing.html"), HttpStatus.FORBIDDEN_403, MimeTypeConstants.MIME_TEXT_HTML),
    
    // this error should be returned when an URL was detected as being a
    // phishing site **before** it was added to the database
    PHISHING_REFUSED("phishing".getBytes(StandardCharsets.US_ASCII), HttpStatus.FORBIDDEN_403, MimeTypeConstants.MIME_TEXT_PLAIN),
    
    // this error should be returned when an URL was detected as being a malware
    // site **after** it was added to the database
    MALWARE(Response.readFile("/403-malware.html"), HttpStatus.FORBIDDEN_403, MimeTypeConstants.MIME_TEXT_HTML), 
    
    // this error should be returned when an URL was detected as being a
    // phishing site **before** it was added to the database
    MALWARE_REFUSED("malware".getBytes(StandardCharsets.US_ASCII), HttpStatus.FORBIDDEN_403, MimeTypeConstants.MIME_TEXT_PLAIN), 
    
    // URL has been submitted and is being processed
    PROCESSING(HttpStatus.ACCEPTED_202), 
    
    // bad request
    BAD_REQUEST(HttpStatus.BAD_REQUEST_400),
    
    // client is asking but we don't supply 
    METHOD_NOT_ALLOWED_405(HttpStatus.METHOD_NOT_ALLOWED_405),

    // something really bad has happened (See where its being used for more shocking details)
    INTERNAL_SERVER_ERROR_500(HttpStatus.INTERNAL_SERVER_ERROR_500);
    
    final MimeTypeConstants mime;
    final private byte[] body;
    final private int status;

    ErrorPages(byte[] body, int status, MimeTypeConstants mime) {
        this.status = status;
        this.body = body;
        this.mime = mime;
    }

    ErrorPages(int status) {
        this(new byte[0],status, MimeTypeConstants.MIME_TEXT_PLAIN);
    }

    @Override
    public int getHttpStatus() {
        return status;
    }

    public String getMimeType() {
        return mime.toString();
    }

    @Override
    public boolean isCacheable() {
        return false;
    }

    @Override
    public void run(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try (ServletOutputStream stream = response.getOutputStream()) {

            stream.write(body);
            stream.flush();
        }
    }
}
