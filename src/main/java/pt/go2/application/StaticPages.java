package pt.go2.application;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pt.go2.fileio.Configuration;
import pt.go2.fileio.EmbeddedFiles;
import pt.go2.fileio.ErrorPages;
import pt.go2.response.AbstractResponse;
import pt.go2.response.RedirectResponse;
import pt.go2.storage.HashKey;
import pt.go2.storage.KeyValueStore;
import pt.go2.storage.Uri;

/**
 * Handles server requests
 */
class StaticPages extends RequestHandler {

    final Calendar calendar = Calendar.getInstance();

    final KeyValueStore ks;
    final EmbeddedFiles files;

    public StaticPages(final Configuration config, ErrorPages errors, KeyValueStore ks, EmbeddedFiles res) {
        super(config, errors);

        this.ks = ks;
        this.files = res;
    }

    /**
     * Handle request, parse URI filename from request into page resource
     *
     * @param
     *
     * @exception IOException
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse exchange) {

        final String requested = getRequestedFilename(request.getRequestURI());

        if (requested.length() == HashKey.LENGTH) {

            final HashKey hk;

            hk = new HashKey(requested);

            final Uri uri = ks.get(hk);

            if (uri == null) {
                reply(request, exchange, ErrorPages.Error.PAGE_NOT_FOUND, true);
                return;
            }

            switch (uri.health()) {
            case PHISHING:
                reply(request, exchange, ErrorPages.Error.PHISHING, true);
                break;
            case OK:
                reply(request, exchange, new RedirectResponse(uri.toString(), config.getRedirect()), true);
                break;
            case MALWARE:
                reply(request, exchange, ErrorPages.Error.MALWARE, true);
                break;
            default:
                reply(request, exchange, ErrorPages.Error.PAGE_NOT_FOUND, true);
            }

            return;
        }

        final AbstractResponse response = files.getFile(requested);

        if (response == null) {
            reply(request, exchange, ErrorPages.Error.PAGE_NOT_FOUND, true);
        } else {
            reply(request, exchange, response, true);
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
}
