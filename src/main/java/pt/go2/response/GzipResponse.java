package pt.go2.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;

/**
 * @author vilaca
 *
 */
public class GzipResponse extends AbstractResponse {

    private static final Logger LOGGER = LogManager.getLogger();

    final byte[] body;
    final byte[] zipBody;
    final String mime;
    private boolean zipped;

    /**
     * c'tor
     *
     * @param body
     * @param mime
     */
    public GzipResponse(byte[] body, String mime) {

        this.body = body;
        this.zipBody = zipBody(body);
        this.mime = mime;
    }

    private byte[] zipBody(byte[] body) {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                GZIPOutputStream zip = new GZIPOutputStream(baos);) {

            zip.write(body);
            zip.flush();
            zip.close();

            return baos.toByteArray();

        } catch (final IOException e) {

            LOGGER.error(e);

            return new byte[0];
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see pt.go2.application.HttpResponse#getHttpErrorCode()
     */
    @Override
    public int getHttpStatus() {
        return HttpStatus.OK_200;
    }

    /*
     * (non-Javadoc)
     *
     * @see pt.go2.application.HttpResponse#getMimeType()
     */
    @Override
    public String getMimeType() {
        return mime;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * pt.go2.application.HttpResponse#getBody(com.sun.net.httpserver.HttpExchange
     * )
     */
    @Override
    public void run(HttpServletResponse exchange) throws IOException {

        final byte[] content;

        if (this.zipBody.length > 0 && clientAcceptsZip(exchange)) {
            zipped = true;
            content = zipBody;
        } else {
            zipped = false;
            content = body;
        }

        try (ServletOutputStream stream = exchange.getOutputStream()) {

            stream.write(content);
            stream.flush();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see pt.go2.application.HttpResponse#isZipped()
     */
    @Override
    public boolean isZipped() {
        return zipped;
    }

    /**
     * Does the client accept a ziped response?
     *
     * @param exchange
     *
     * @return
     */
    private boolean clientAcceptsZip(final HttpServletResponse exchange) {

        final String header = exchange.getHeader(REQUEST_HEADER_ACCEPT_ENCODING);

        return header != null && header.indexOf("gzip") != -1;
    }

    @Override
    protected byte[] getBody() {
        return new byte[0];
    }
}
