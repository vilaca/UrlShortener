package pt.go2.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
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
import pt.go2.response.Response;
import pt.go2.response.GenericResponse;
import pt.go2.response.RedirectResponse;
import pt.go2.storage.HashKey;
import pt.go2.storage.KeyValueStore;
import pt.go2.storage.Uri;
import pt.go2.storage.Uri.Health;

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
        
        response.setHeader(Response.RESPONSE_HEADER_SERVER, "Carapau de corrida " + config.getVersion());

        response.setHeader(Response.RESPONSE_HEADER_CONTENT_TYPE, file.getMimeType());

        if (file.isCacheable()) {

            response.setHeader(Response.RESPONSE_HEADER_CACHE_CONTROL,
                    "max-age=" + TimeUnit.HOURS.toSeconds(config.getCacheHint()));

        } else {
            response.setHeader(Response.RESPONSE_HEADER_CACHE_CONTROL, "no-cache, no-store, must-revalidate");

            response.setHeader(Response.RESPONSE_HEADER_EXPIRES, "0");
        }

        if (file.isZipped()) {
            response.setHeader(Response.RESPONSE_HEADER_CONTENT_ENCODING, "gzip");
        }


        try (ServletOutputStream stream = response.getOutputStream()) {

            response.setStatus(file.getHttpStatus());

            file.run(response);

            stream.write(file.getBody());
            stream.flush();
            
        } catch (final IOException e) {

            LOG.error("Error while streaming the response.", e);

            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }

        accessLog.log(response.getStatus(), request, response.getBufferSize());

    }
    
    public Response _handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        // we need a host header to continue

        String host = request.getHeader(Response.REQUEST_HEADER_HOST);

        if (host == null || host.isEmpty()) {
            return ErrorPages.BAD_REQUEST;
        }

        host = host.toLowerCase();

        final String requested = getRequestedFilename(request.getRequestURI());

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

                    return new RedirectResponse(redirect, HttpStatus.MOVED_PERMANENTLY_301);
                }
            }
        }

        if (request.getMethod().equals(HttpMethod.GET.toString())) {

            if (requested.length() == HashKey.LENGTH) {

                return handleShortenedUrl(request, response, requested.getBytes());

            } else {

                final String acceptedEncoding = request.getHeader(Response.REQUEST_HEADER_ACCEPT_ENCODING);

                // TODO pack200-gzip false positive

                final boolean gzip = acceptedEncoding != null && acceptedEncoding.contains("gzip");

                final Response file = files.getFile(requested, gzip);

                if (file == null) {

                    return ErrorPages.PAGE_NOT_FOUND;

                } else {

                    return file;
                }
            }

        } else if (request.getMethod().equals(HttpMethod.POST.toString()) && "new".equals(requested)) {

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
                return GenericResponse.create(hk.getHash(), Response.MIME_TEXT_PLAIN);
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

    /**
     * Parse requested filename from URI
     *
     * @param path
     *
     * @return Requested filename
     */
    private String getRequestedFilename(String path) {

        // split into tokens

        if (path.isEmpty() || "/".equals(path)) {
            return "/";
        }

        final int idx = path.indexOf("/", 1);

        return idx == -1 ? path.substring(1) : path.substring(1, idx);
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
            return new RedirectResponse(uri.toString(), config.getRedirect());
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
