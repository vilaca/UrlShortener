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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import pt.go2.external.UrlHealth;
import pt.go2.fileio.AccessLogger;
import pt.go2.fileio.Configuration;
import pt.go2.storage.HashKey;
import pt.go2.storage.KeyValueStore;
import pt.go2.storage.Uri;
import pt.go2.storage.Uri.Health;

import static pt.go2.application.HeaderConstants.*;

class RequestHandler extends AbstractHandler {

    // only for access_log file

    private static final Logger LOG = LogManager.getLogger();

    private final AccessLogger accessLog = new AccessLogger();

    private final KeyValueStore ks;
    private final EmbeddedPages files;
    private final UrlHealth health;

    private final Configuration config;

    public RequestHandler(Configuration conf, KeyValueStore ks, EmbeddedPages files, UrlHealth health) {
        this.config = conf;
        this.ks = ks;
        this.files = files;
        this.health = health;
    }

    @Override
    public void handle(String dontcare, Request base, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        base.setHandled(true);
        
        Response file = _handle(request, response);
        
        setHeader(response, RESPONSE_HEADER_SERVER, "Carapau de corrida " + config.getVersion());

        setHeader(response, RESPONSE_HEADER_CONTENT_TYPE, file.getMimeType());

        if (file.isCacheable()) {

            setHeader(response, RESPONSE_HEADER_CACHE_CONTROL,
                    "max-age=" + TimeUnit.HOURS.toSeconds(config.getCacheHint()));

        } else {
            setHeader(response, RESPONSE_HEADER_CACHE_CONTROL, "no-cache, no-store, must-revalidate");

            setHeader(response, RESPONSE_HEADER_EXPIRES, "0");
        }

        try {

            response.setStatus(file.getHttpStatus());

            file.run(request, response);

            
        } catch (final IOException e) {

            LOG.error("Error while streaming the response.", e);

            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }

        final String referer = getHeader(request, REQUEST_HEADER_REFERER);

        final String agent = getHeader(request, REQUEST_HEADER_USER_AGENT);

        accessLog.log(response.getStatus(), request, response.getBufferSize(), referer, agent);
    }
    
    private void setHeader(HttpServletResponse response, HeaderConstants header, String value) {
        response.setHeader(header.toString(), value);
    }

    public Response _handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        // we need a host header to continue

        String host = getHeader(request, REQUEST_HEADER_HOST);

        if (host == null || host.isEmpty()) {
            return ErrorPages.BAD_REQUEST;
        }

        host = host.toLowerCase();

        final String requested = request.getRequestURI();

        if (!config.getValidDomains().isEmpty()) {

            if (!config.getValidDomains().contains(host)) {

                LOG.error("Wrong host: " + host);

                return ErrorPages.BAD_REQUEST;
            }

            if (request.getMethod().equals(HttpMethod.GET.toString())) {

                // if its not a shortened URL that was requested, make sure
                // the preferred name is being used (ie www.go2.pt vs go2.pt)

                final String preffered = config.getValidDomains().get(0);

                if (!host.equals(preffered)) {

                    // TODO support HTTPS too

                    final String redirect = "http://" + preffered
                            + (requested.startsWith("/") ? requested : "/" + requested);

                    LOG.error("Use preffered hostname. Redirected from " + host + " to " + redirect);

                    return ResponseFactory.create(redirect, HttpStatus.MOVED_PERMANENTLY_301);
                }
            }
        }

        if (request.getMethod().equals(HttpMethod.GET.toString())) {

            if (requested.length() == HashKey.LENGTH) {

                return handleShortenedUrl(request, response, requested.getBytes());

            } else {

                final Response file = files.getFile(requested);

                if (file == null) {

                    return ErrorPages.PAGE_NOT_FOUND;

                } else {

                    return file;
                }
            }

        } else if (request.getMethod().equals(HttpMethod.POST.toString()) && "/new".equals(requested)) {

            final String field = urltoHash(request, response);

            if ( field == null)
            {
                return ErrorPages.BAD_REQUEST;
            }
            
            Uri uri = Uri.create(field, true, Health.PROCESSING);

            if (uri == null) {
                return ErrorPages.BAD_REQUEST;
            }

            // try to find hash for url is ks

            final HashKey hk = ks.find(uri);

            if (hk == null) {

                return createNewHash(request, response, uri);
            }

            uri = ks.get(hk);

            switch (uri.health()) {
            case MALWARE:                
                return ErrorPages.MALWARE_REFUSED;
            case OK:
                return ResponseFactory.create(hk.getHash());
            case PHISHING:
                return ErrorPages.PHISHING;
            case PROCESSING:
                return ErrorPages.PROCESSING;
            default:
                return ErrorPages.INTERNAL_SERVER_ERROR_500;
            }

        } else {
            
            LOG.error("Method not allowed: " + request.getMethod());
            
            return ErrorPages.METHOD_NOT_ALLOWED_405;
        }
    }

    private String getHeader(HttpServletRequest request, HeaderConstants header) {
        return request.getHeader(header.toString());
    }

    private Response handleShortenedUrl(HttpServletRequest request, HttpServletResponse response, final byte[] requested) {

        final Uri uri = ks.get(new HashKey(requested));

        if (uri == null) {
            return ErrorPages.PAGE_NOT_FOUND;
        }

        switch (uri.health()) {
        case PHISHING:
            return ErrorPages.PHISHING;
        case OK:
            return ResponseFactory.create(uri.toString(), config.getRedirect());
        case MALWARE:
            return ErrorPages.MALWARE;
        case PROCESSING:
            return ErrorPages.PROCESSING;
        }
        
        // TODO log error
        
        return ErrorPages.INTERNAL_SERVER_ERROR_500;
    }

    /**
     * Get URL to hash from POST request
     *
     * @param request
     * @param response
     * @return
     */
    private String urltoHash(HttpServletRequest request, HttpServletResponse response) {

        try (final InputStream is = request.getInputStream();
                final InputStreamReader sr = new InputStreamReader(is, "UTF-8");
                final BufferedReader br = new BufferedReader(sr);) {

            // read body content

            final String postBody = br.readLine();

            if (postBody == null) {
                return null;
            }

            // format for form content is 'fieldname=value'

            final int idx = postBody.indexOf('=') + 1;

            if (idx == -1 || postBody.length() - idx < 3) {
                return null;
            }

            // Parse string into Uri

            return postBody.substring(idx);

        } catch (final IOException e) {

            LOG.error(e);
        }
        
        return null;
    }

    private Response createNewHash(HttpServletRequest request, HttpServletResponse response, Uri uri) {

        // hash not found, add new

        if (!ks.add(uri)) {
            return ErrorPages.INTERNAL_SERVER_ERROR_500;
        }

        health.test(uri, true);

        if (uri.health() == Health.PROCESSING) {
            uri.setHealth(Health.OK);
        }

        return ErrorPages.PROCESSING;
    }
}
