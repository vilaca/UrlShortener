package pt.go2.mocks;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class HttpServletResponseMock implements HttpServletResponse {

    final Map<String, String> headers = new HashMap<>();

    private int status, written;

    @Override
    public void flushBuffer() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getBufferSize() {
        return written;
    }

    @Override
    public String getCharacterEncoding() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContentType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {

            @Override
            public boolean isReady() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void setWriteListener(WriteListener arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void write(int arg0) throws IOException {
                // TODO Auto-generated method stub
            }

            @Override
            public void write(byte[] array) throws IOException {
                written = array.length;
            }
        };
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCommitted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetBuffer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBufferSize(int arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCharacterEncoding(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setContentLength(int arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setContentLengthLong(long arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setContentType(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLocale(Locale arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addCookie(Cookie arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addDateHeader(String arg0, long arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addHeader(String arg0, String arg1) {
        headers.put(arg0, arg1);
    }

    @Override
    public void addIntHeader(String arg0, int arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsHeader(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeRedirectURL(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeRedirectUrl(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeURL(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeUrl(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getHeader(String arg0) {
        return headers.get(arg0);
    }

    @Override
    public Collection<String> getHeaderNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getHeaders(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void sendError(int arg0) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendError(int arg0, String arg1) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendRedirect(String arg0) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDateHeader(String arg0, long arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeader(String arg0, String arg1) {
        addHeader(arg0, arg1);
    }

    @Override
    public void setIntHeader(String arg0, int arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public void setStatus(int arg0, String arg1) {
        throw new UnsupportedOperationException();
    }

}
