package pt.go2.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.jetty.http.HttpStatus;

public class GenericResponse extends AbstractResponse {

    private static final int BUFFER_SIZE = 4096;

    final byte[] body;
    final int error;
    final String mime;

    public GenericResponse(String content, String encoding, int error, String mime) throws UnsupportedEncodingException {
        this.body = content.getBytes(encoding);
        this.error = error;
        this.mime = mime;
    }

    public GenericResponse(int error) {
        this.body = new byte[0];
        this.error = error;
        this.mime = MIME_TEXT_PLAIN;
    }

    public GenericResponse(String content, String encoding) throws UnsupportedEncodingException {
        this.body = content.getBytes(encoding);
        this.error = HttpStatus.OK_200;
        this.mime = MIME_TEXT_PLAIN;
    }

    public GenericResponse(InputStream is) throws IOException {

        final byte[] buffer = new byte[BUFFER_SIZE];
        int read;

        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        while ((read = is.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }

        this.body = output.toByteArray();
        this.error = HttpStatus.NOT_FOUND_404;
        this.mime = MIME_TEXT_PLAIN;
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
    protected byte[] getBody() {
        return body;
    }
}
