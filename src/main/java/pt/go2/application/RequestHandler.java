package pt.go2.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
import pt.go2.fileio.EmbeddedFiles;
import pt.go2.fileio.ErrorPages;
import pt.go2.response.AbstractResponse;
import pt.go2.response.GenericResponse;
import pt.go2.response.RedirectResponse;
import pt.go2.storage.HashKey;
import pt.go2.storage.KeyValueStore;
import pt.go2.storage.Uri;
import pt.go2.storage.Uri.Health;

class RequestHandler extends AbstractHandler {

    // only for access_log file

    private static final AccessLogger ACCESSLOG = new AccessLogger();

    private static final Logger LOG = LogManager.getLogger();

    private final KeyValueStore ks;
    private final EmbeddedFiles files;
    private final UrlHealth health;

    private final Configuration config;
    private final ErrorPages errors;

    public RequestHandler(Configuration conf, ErrorPages err, KeyValueStore ks, EmbeddedFiles files, UrlHealth health) {
        this.config = conf;
        this.errors = err;
        this.ks = ks;
        this.files = files;
        this.health = health;
    }

    @Override
    public void handle(String dontcare, Request base, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        base.setHandled(true);

        // we need a host header to continue

        String host = request.getHeader(AbstractResponse.REQUEST_HEADER_HOST);

        if (host == null || host.isEmpty()) {
            reply(request, response, GenericResponse.NotOk(HttpStatus.BAD_REQUEST_400), false);
            return;
        }

        host = host.toLowerCase();

        final String requested = getRequestedFilename(request.getRequestURI());

        if (!config.getValidDomains().isEmpty()) {

            if (!config.getValidDomains().contains(host)) {

                reply(request, response, GenericResponse.NotOk(HttpStatus.BAD_REQUEST_400), true);

                LOG.error("Wrong host: " + host);

                return;
            }

            if (request.getMethod().equals(HttpMethod.GET.toString())) {

                // if its not a shortened URL that was requested, make sure
                // the preferred name is being used (ie www.go2.pt vs go2.pt)

                final String preffered = config.getValidDomains().get(0);

                if (!host.equals(preffered)) {

                    // TODO support HTTPS too

                    final String redirect = "http://" + preffered
                            + (requested.startsWith("/") ? request : "/" + requested);

                    reply(request, response, new RedirectResponse(redirect, HttpStatus.MOVED_PERMANENTLY_301), true);

                    LOG.error("Use preffered hostname. Redirected from " + host + " to " + redirect);

                    return;
                }
            }
        }

        if (request.getMethod().equals(HttpMethod.GET.toString())) {

            if (requested.length() == HashKey.LENGTH) {

                handleShortenedUrl(request, response, requested.getBytes());

                return;

            } else {

                final String accept = request.getHeader(AbstractResponse.REQUEST_HEADER_ACCEPT_ENCODING);

                // TODO pack200-gzip false positive

                final boolean gzip = accept != null && accept.contains("gzip");

                final AbstractResponse file = files.getFile(requested, gzip);

                if (response == null) {

                    reply(request, response, errors.get(ErrorPages.Error.PAGE_NOT_FOUND), true);

                } else {

                    reply(request, response, file, true);
                }
            }

        } else if (request.getMethod().equals(HttpMethod.POST.toString()) && "new".equals(requested)) {

            final String field = urltoHash(request, response);

            Uri uri = Uri.create(field, true, Health.PROCESSING);

            if (uri == null) {
                reply(request, response, GenericResponse.NotOk(HttpStatus.BAD_REQUEST_400), false);
                return;
            }

            // try to find hash for url is ks

            final HashKey hk = ks.find(uri);

            if (hk == null) {

                createNewHash(request, response, uri);

                return;
            }

            uri = ks.get(hk);

            switch (uri.health()) {
            case MALWARE:
                reply(request, response, GenericResponse.createForbidden("malware".getBytes(StandardCharsets.US_ASCII)),
                        true);
                break;
            case OK:
                reply(request, response, GenericResponse.create(hk.getHash(), AbstractResponse.MIME_TEXT_PLAIN), false);
                break;
            case PHISHING:
                reply(request, response,
                        GenericResponse.createForbidden("phishing".getBytes(StandardCharsets.US_ASCII)), true);
                break;
            case PROCESSING:
                reply(request, response, GenericResponse.NotOk(HttpStatus.ACCEPTED_202), false);
                break;
            default:
                reply(request, response, GenericResponse.NotOk(HttpStatus.INTERNAL_SERVER_ERROR_500), false);
                break;
            }

        } else {
            reply(request, response, GenericResponse.NotOk(HttpStatus.METHOD_NOT_ALLOWED_405), true);
            LOG.error("Method not allowed: " + request.getMethod());
        }
    }

    /**
     * Stream Http Response
     *
     * @param request
     *
     * @param exchange
     * @param response
     * @throws IOException
     */
    protected void reply(HttpServletRequest request, final HttpServletResponse exchange,
            final AbstractResponse response, final boolean cache) {

        setHeaders(exchange, response, cache);

        try {

            exchange.setStatus(response.getHttpStatus());

            response.run(exchange);

        } catch (final IOException e) {

            LOG.error("Error while streaming the response.", e);

            exchange.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }

        ACCESSLOG.log(exchange.getStatus(), request, exchange.getBufferSize());
    }

    /**
     * Set response headers
     *
     * @param exchange
     *
     * @param response
     * @param headers
     */
    private void setHeaders(HttpServletResponse exchange, final AbstractResponse response, final boolean cache) {

        exchange.setHeader(AbstractResponse.RESPONSE_HEADER_SERVER, "Carapau de corrida " + config.getVersion());

        exchange.setHeader(AbstractResponse.RESPONSE_HEADER_CONTENT_TYPE, response.getMimeType());

        if (cache) {

            exchange.setHeader(AbstractResponse.RESPONSE_HEADER_CACHE_CONTROL,
                    "max-age=" + TimeUnit.HOURS.toSeconds(config.getCacheHint()));

        } else {
            exchange.setHeader(AbstractResponse.RESPONSE_HEADER_CACHE_CONTROL, "no-cache, no-store, must-revalidate");

            exchange.setHeader(AbstractResponse.RESPONSE_HEADER_EXPIRES, "0");
        }

        if (response.isZipped()) {
            exchange.setHeader(AbstractResponse.RESPONSE_HEADER_CONTENT_ENCODING, "gzip");
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

    private void handleShortenedUrl(HttpServletRequest request, HttpServletResponse response, final byte[] requested) {

        final Uri uri = ks.get(new HashKey(requested));

        if (uri == null) {
            reply(request, response, errors.get(ErrorPages.Error.PAGE_NOT_FOUND), true);
            return;
        }

        switch (uri.health()) {
        case PHISHING:
            reply(request, response, errors.get(ErrorPages.Error.PHISHING), true);
            break;
        case OK:
            reply(request, response, new RedirectResponse(uri.toString(), config.getRedirect()), true);
            break;
        case MALWARE:
            reply(request, response, errors.get(ErrorPages.Error.MALWARE), true);
            break;
        default:
            reply(request, response, errors.get(ErrorPages.Error.PAGE_NOT_FOUND), true);
        }
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
                reply(request, response, GenericResponse.NotOk(HttpStatus.BAD_REQUEST_400), false);
                return null;
            }

            // format for form content is 'fieldname=value'

            final int idx = postBody.indexOf('=') + 1;

            if (idx == -1 || postBody.length() - idx < 3) {
                reply(request, response, GenericResponse.NotOk(HttpStatus.BAD_REQUEST_400), false);
                return null;
            }

            // Parse string into Uri

            return postBody.substring(idx);

        } catch (final IOException e) {

            LOG.error(e);

            reply(request, response, GenericResponse.NotOk(HttpStatus.BAD_REQUEST_400), false);

            return null;
        }
    }

    private void createNewHash(HttpServletRequest request, HttpServletResponse response, Uri uri) {

        // hash not found, add new

        if (!ks.add(uri)) {
            reply(request, response, GenericResponse.NotOk(HttpStatus.INTERNAL_SERVER_ERROR_500), false);
            return;
        }

        reply(request, response, GenericResponse.NotOk(HttpStatus.ACCEPTED_202), false);

        health.test(uri, true);

        if (uri.health() == Health.PROCESSING) {
            uri.setHealth(Health.OK);
        }

        return;
    }
}
