package pt.go2.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

import pt.go2.response.Response;

public enum ErrorPages implements Response {

    PAGE_NOT_FOUND(readStream(ErrorPages.class.getResourceAsStream("/404.html")), 404),
    
    // this error should be returned when an URL was detected as being a
    // phishing site **after** it was added to the database
    PHISHING(readStream(ErrorPages.class.getResourceAsStream("/403-phishing.html")), 403),
    
    // this error should be returned when an URL was detected as being a
    // phishing site **before** it was added to the database
    PHISHING_REFUSED("phishing".getBytes(StandardCharsets.US_ASCII), 403),
    
    // this error should be returned when an URL was detected as being a malware
    // site **after** it was added to the database
    MALWARE(readStream(ErrorPages.class.getResourceAsStream("/403-malware.html")), 403), BAD_REQUEST(new byte[0], 400),

    // this error should be returned when an URL was detected as being a
    // phishing site **before** it was added to the database
    MALWARE_REFUSED("malware".getBytes(StandardCharsets.US_ASCII), 403), 
    
    // URL has been submitted and is being processed
    PROCESSING(HttpStatus.ACCEPTED_202), 
    
    // something really bad has happened (See where its being used for more shocking details)
    INTERNAL_SERVER_ERROR_500(HttpStatus.INTERNAL_SERVER_ERROR_500), 
    
    // client is asking but we don't supply 
    METHOD_NOT_ALLOWED_405(HttpStatus.METHOD_NOT_ALLOWED_405);

    
    private int status;
    private byte[] body;

    ErrorPages(byte[] body, int status) {
        this.status = status;
        this.body = body;
    }

    ErrorPages(int status) {
        this(new byte[0],status);
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

            // TODO what's best to use here? i forgot..

            throw new ExceptionInInitializerError(e);
        }

        return output.toByteArray();
    }

    @Override
    public int getHttpStatus() {
        return status;
    }

    @Override
    public byte[] getBody() {
        return this.body;
    }

    @Override
    public void run(HttpServletResponse exchange) throws IOException {
    }

    @Override
    public String getMimeType() {
        return MIME_TEXT_HTML;
    }

    @Override
    public boolean isZipped() {
        return false;
    }

    @Override
    public boolean isCacheable() {
        return false;
    }
}
