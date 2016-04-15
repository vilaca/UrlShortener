package pt.go2.response;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

public class GenericResponse implements Response {

    final byte[] body;
    final int error;
    final String mime;

    public static GenericResponse create(byte[] body, String mime) {

        return new GenericResponse(body, HttpStatus.OK_200, mime);
    }

    private GenericResponse(byte[] body, int error, String mime) {

        this.body = body;
        this.error = error;
        this.mime = mime;
    }

    @Override
    public int getHttpStatus() {
        return error;
    }

    @Override
    public String getMimeType() {
        return mime;
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public void run(HttpServletResponse exchange) throws IOException {
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
