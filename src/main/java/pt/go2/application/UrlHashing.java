package pt.go2.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;

import pt.go2.external.UrlHealth;
import pt.go2.fileio.Configuration;
import pt.go2.fileio.ErrorPages;
import pt.go2.response.AbstractResponse;
import pt.go2.response.GenericResponse;
import pt.go2.storage.HashKey;
import pt.go2.storage.KeyValueStore;
import pt.go2.storage.Uri;
import pt.go2.storage.Uri.Health;

class UrlHashing extends RequestHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final int MINIMUM_URL_SIZE = 3;

    final KeyValueStore ks;
    final UrlHealth health;

    public UrlHashing(Configuration config, ErrorPages errors, KeyValueStore ks, UrlHealth health) {

        super(config, errors);

        this.ks = ks;
        this.health = health;
    }

    /**
     * Handle shortening of Urls.
     *
     * If Url already exists return hash. If Url wasn't hashed before generate
     * hash and add it to value store
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) {

        Uri uri = urltoHash(request, response);

        if (uri == null) {
            return;
        }

        // try to find hash for url is ks

        final HashKey hk = ks.find(uri);

        if (hk == null) {

            // hash not found, add new

            if (!ks.add(uri)) {
                reply(request, response, new GenericResponse(HttpStatus.INTERNAL_SERVER_ERROR_500), false);
                return;
            }

            reply(request, response, new GenericResponse(HttpStatus.ACCEPTED_202), false);
            health.test(uri, true);

            if (uri.health() == Health.PROCESSING) {
                uri.setHealth(Health.OK);
            }

            return;
        }

        uri = ks.get(hk);

        try {
            switch (uri.health()) {
            case MALWARE:
                reply(request, response, new GenericResponse("malware", "US-ASCII", HttpStatus.FORBIDDEN_403,
                        AbstractResponse.MIME_TEXT_PLAIN), true);
                break;
            case OK:
                reply(request, response, new GenericResponse(hk.toString(), "US-ASCII"), false);
                break;
            case PHISHING:
                reply(request, response, new GenericResponse("phishing", "US-ASCII", HttpStatus.FORBIDDEN_403,
                        AbstractResponse.MIME_TEXT_PLAIN), true);
                break;
            case PROCESSING:
                reply(request, response, new GenericResponse(HttpStatus.ACCEPTED_202), false);
                break;
            default:
                reply(request, response, new GenericResponse(HttpStatus.INTERNAL_SERVER_ERROR_500), false);
                break;
            }
        } catch (final UnsupportedEncodingException e) {
            reply(request, response, new GenericResponse(HttpStatus.INTERNAL_SERVER_ERROR_500), false);
            LOGGER.error(e);
        }
    }

    /**
     * Get URL to hash from POST request
     *
     * @param request
     * @param response
     * @return
     */
    private Uri urltoHash(HttpServletRequest request, HttpServletResponse response) {

        try (final InputStream is = request.getInputStream();
                final InputStreamReader sr = new InputStreamReader(is, "UTF-8");
                final BufferedReader br = new BufferedReader(sr);) {

            // read body content

            final String postBody = br.readLine();

            if (postBody == null) {
                reply(request, response, new GenericResponse(HttpStatus.BAD_REQUEST_400), false);
                return null;
            }

            // format for form content is 'fieldname=value'

            final int idx = postBody.indexOf('=') + 1;

            if (idx == -1 || postBody.length() - idx < MINIMUM_URL_SIZE) {
                reply(request, response, new GenericResponse(HttpStatus.BAD_REQUEST_400), false);
                return null;
            }

            // Parse string into Uri

            final Uri uri = Uri.create(postBody.substring(idx), true, Health.PROCESSING);

            if (uri == null) {
                reply(request, response, new GenericResponse(HttpStatus.BAD_REQUEST_400), false);
            }

            return uri;

        } catch (final IOException e) {

            LOGGER.error(e);
            reply(request, response, new GenericResponse(HttpStatus.BAD_REQUEST_400), false);
        }

        return null;
    }
}
