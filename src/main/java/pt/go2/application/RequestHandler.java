package pt.go2.application;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import pt.go2.fileio.AccessLogger;
import pt.go2.fileio.Configuration;
import pt.go2.fileio.ErrorPages;
import pt.go2.response.AbstractResponse;
import pt.go2.response.GenericResponse;

public abstract class RequestHandler extends AbstractHandler {

    // only for access_log file
	private static final AccessLogger ACCESSLOG = new AccessLogger();

    private static final Logger LOG = LogManager.getLogger();

    protected final Configuration config;
    private final ErrorPages errors;

    public RequestHandler(Configuration config, ErrorPages errors) {

        this.config = config;
        this.errors = errors;
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

    protected void reply(HttpServletRequest request, HttpServletResponse exchange, ErrorPages.Error badRequest,
            boolean cache) {
        reply(request, exchange, errors.get(badRequest), cache);
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

    @Override
    public void handle(String dontcare, Request base, HttpServletRequest request, HttpServletResponse response) {

        base.setHandled(true);

        // we need a host header to continue

        String host = request.getHeader(AbstractResponse.REQUEST_HEADER_HOST);

        if (host == null || host.isEmpty()) {
            reply(request, response, new GenericResponse(HttpStatus.BAD_REQUEST_400), false);
            return;
        }

        host = host.toLowerCase();

        if (!config.getValidDomains().isEmpty()) {

            // redirect to domain if host header is not correct

            if (!config.getValidDomains().contains(host)) {
            	
                reply(request, response, new GenericResponse(HttpStatus.BAD_REQUEST_400), true);
                LOG.error("Wrong host: " + host);
                return;
            }

        }

        handle(request, response);
    }

    public abstract void handle(HttpServletRequest req, HttpServletResponse res);
}
