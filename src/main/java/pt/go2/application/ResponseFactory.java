package pt.go2.application;

import static pt.go2.application.HeaderConstants.REQUEST_HEADER_ACCEPT_ENCODING;
import static pt.go2.application.HeaderConstants.RESPONSE_HEADER_CONTENT_ENCODING;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

public class ResponseFactory {

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

    public static Response create(int status, String mime, byte[] zipped, byte[] content) {

        return new Response() {

            @Override
            public int getHttpStatus() {
                return status;
            }

            @Override
            public void run(HttpServletRequest request, HttpServletResponse response) throws IOException {

                final String acceptedEncoding = request.getHeader(REQUEST_HEADER_ACCEPT_ENCODING.toString());

                // TODO pack200-gzip false positive

                final boolean gzip = acceptedEncoding != null && acceptedEncoding.contains("gzip");

                response.setHeader(RESPONSE_HEADER_CONTENT_ENCODING.toString(), "gzip");

                try (ServletOutputStream stream = response.getOutputStream()) {

                    stream.write(gzip ? zipped : content);
                    stream.flush();
                }
            }

            @Override
            public String getMimeType() {
                return mime;
            }

            @Override
            public boolean isCacheable() {
                return true;
            }
        };
    }

    public static Response create(String redirect, int status) {

        return new Response(){

            @Override
            public int getHttpStatus() {
                return status;
            }

            @Override
            public void run(HttpServletRequest request, HttpServletResponse response) throws IOException {
                response.setHeader(HeaderConstants.RESPONSE_HEADER_LOCATION.toString(), redirect);
            }

            @Override
            public String getMimeType() {
                return HeaderConstants.MIME_TEXT_PLAIN.toString();
            }

            @Override
            public boolean isCacheable() {
                return true;
            }};
    }

    public static Response create(byte[] body) {

        return new Response(){

            @Override
            public int getHttpStatus() {
                return HttpStatus.OK_200;
            }

            @Override
            public void run(HttpServletRequest request, HttpServletResponse response) throws IOException {

                try (ServletOutputStream stream = response.getOutputStream()) {

                    stream.write(body);
                    stream.flush();
                }
            }

            @Override
            public String getMimeType() {
                return HeaderConstants.MIME_TEXT_PLAIN.toString();
            }

            @Override
            public boolean isCacheable() {
                return true;
            }};
    }
}
