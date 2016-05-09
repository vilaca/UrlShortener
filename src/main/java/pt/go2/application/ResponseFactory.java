/*
    Copyright (C) 2016 João Vilaça

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
*/
package pt.go2.application;

import static pt.go2.application.HeaderConstants.REQUEST_HEADER_ACCEPT_ENCODING;
import static pt.go2.application.HeaderConstants.RESPONSE_HEADER_CONTENT_ENCODING;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

class ResponseFactory {

    public static Response create(final int status, final MimeTypeConstants mime, final boolean cacheable, final byte[] content) {

        return new Response() {

            @Override
            public boolean isCacheable() {
                return cacheable;
            }

            @Override
            public String getMimeType() {
                return mime.toString();
            }

            @Override
            public int getHttpStatus() {
                return status;
            }

            @Override
            public void run(HttpServletRequest request, HttpServletResponse response) throws IOException {

                try (ServletOutputStream stream = response.getOutputStream()) {

                    stream.write(content);
                    stream.flush();
                }
            }
        };
    }

    public static Response create(final int status, final MimeTypeConstants mime, final byte[] zipped, final byte[] content) {

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
                return mime.toString();
            }

            @Override
            public boolean isCacheable() {
                return true;
            }
        };
    }

    public static Response create(final String redirect, final int status) {

        return new Response() {

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
                return MimeTypeConstants.MIME_TEXT_PLAIN.toString();
            }

            @Override
            public boolean isCacheable() {
                return true;
            }
        };
    }

    public static Response create(final byte[] content) {

        return new Response() {

            @Override
            public int getHttpStatus() {
                return HttpStatus.OK_200;
            }

            @Override
            public void run(HttpServletRequest request, HttpServletResponse response) throws IOException {

                try (ServletOutputStream stream = response.getOutputStream()) {

                    stream.write(content);
                    stream.flush();
                }
            }

            @Override
            public String getMimeType() {
                return MimeTypeConstants.MIME_TEXT_PLAIN.toString();
            }

            @Override
            public boolean isCacheable() {
                return true;
            }
        };
    }
}
