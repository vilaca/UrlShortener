package pt.go2.application;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract class for server response
 */
public interface Response {

    /**
     * Http Status code for response
     *
     * @return
     */
    public int getHttpStatus();

    /**
     * Generate response
     *
     * @param exchange
     * @return
     * @throws IOException
     */
    public void run(HttpServletRequest request, HttpServletResponse response) throws IOException;

    public String getMimeType();

    public boolean isCacheable();

    public static Response create(int status, HeaderConstants mimeTextPlain, boolean cacheable, byte[] body) {
        
        return new Response() {

            @Override
            public boolean isCacheable() {
                return cacheable;
            }

            @Override
            public String getMimeType() {
                return mimeTextPlain.toString();
            }

            @Override
            public int getHttpStatus() {
                return status;
            }

            @Override
            public void run(HttpServletRequest request, HttpServletResponse response) throws IOException {

                try (ServletOutputStream stream = response.getOutputStream()) {

                    stream.write(body);
                    stream.flush();
                }
            }
        };
    }
}