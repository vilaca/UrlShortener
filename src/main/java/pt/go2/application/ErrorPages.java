package pt.go2.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

enum ErrorPages implements Response {

    PAGE_NOT_FOUND(readStream(ErrorPages.class.getResourceAsStream("/404.html")), HttpStatus.NOT_FOUND_404, HeaderConstants.MIME_TEXT_HTML),
    
    // this error should be returned when an URL was detected as being a
    // phishing site **after** it was added to the database
    PHISHING(readStream(ErrorPages.class.getResourceAsStream("/403-phishing.html")), HttpStatus.FORBIDDEN_403, HeaderConstants.MIME_TEXT_HTML),
    
    // this error should be returned when an URL was detected as being a
    // phishing site **before** it was added to the database
    PHISHING_REFUSED("phishing".getBytes(StandardCharsets.US_ASCII), HttpStatus.FORBIDDEN_403, HeaderConstants.MIME_TEXT_PLAIN),
    
    // this error should be returned when an URL was detected as being a malware
    // site **after** it was added to the database
    MALWARE(readStream(ErrorPages.class.getResourceAsStream("/403-malware.html")), HttpStatus.FORBIDDEN_403, HeaderConstants.MIME_TEXT_HTML), 
    
    // this error should be returned when an URL was detected as being a
    // phishing site **before** it was added to the database
    MALWARE_REFUSED("malware".getBytes(StandardCharsets.US_ASCII), HttpStatus.FORBIDDEN_403, HeaderConstants.MIME_TEXT_PLAIN), 
    
    // URL has been submitted and is being processed
    PROCESSING(HttpStatus.ACCEPTED_202), 
    
    // bad request
    BAD_REQUEST(HttpStatus.BAD_REQUEST_400),
    
    // client is asking but we don't supply 
    METHOD_NOT_ALLOWED_405(HttpStatus.METHOD_NOT_ALLOWED_405),

    // something really bad has happened (See where its being used for more shocking details)
    INTERNAL_SERVER_ERROR_500(HttpStatus.INTERNAL_SERVER_ERROR_500);
    
    final HeaderConstants mime;
    final private byte[] body;
    final private int status;

    ErrorPages(byte[] body, int status, HeaderConstants mime) {
        this.status = status;
        this.body = body;
        this.mime = mime;
    }

    ErrorPages(int status) {
        this(new byte[0],status, HeaderConstants.MIME_TEXT_PLAIN);
    }

    private static byte[] readStream(InputStream in) {

        final byte[] buffer = new byte[4096];
        int read;

        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            while ((read = in.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        } catch (IOException e) {

            // TODO what's best to use here?

            throw new ExceptionInInitializerError(e);
        }

        return output.toByteArray();
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
