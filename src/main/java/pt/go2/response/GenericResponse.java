package pt.go2.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

public class GenericResponse implements Response {

    private static final int BUFFER_SIZE = 4096;

    final byte[] body;
    final int error;
    final String mime;

    public static GenericResponse createFromFile(InputStream is, int error) throws IOException {

        final byte[] buffer = new byte[BUFFER_SIZE];
        int read;

        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        while ((read = is.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }

        return new GenericResponse(output.toByteArray(), error, MIME_TEXT_HTML);
    }

    public static GenericResponse create(byte[] body, String mime) {

        return new GenericResponse(body, HttpStatus.OK_200, mime);
    }

    public static GenericResponse NotOk(int status) {

        return new GenericResponse(new byte[0], status, MIME_TEXT_HTML);
    }

    public static GenericResponse createForbidden(byte[] body) {

        return new GenericResponse(body, HttpStatus.FORBIDDEN_403, MIME_TEXT_HTML);
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
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isZipped() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCacheable() {
        // TODO Auto-generated method stub
        return false;
    }
}
