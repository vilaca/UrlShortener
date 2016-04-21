package pt.go2.application;

import java.io.IOException;

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
}