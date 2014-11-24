package pt.go2.response;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * Http Redirect
 */
public class RedirectResponse extends AbstractResponse {

    private final String redirect;
    private final int status;

    public RedirectResponse(final String redirect, final int status) {

        this.redirect = redirect;
        this.status = status;
    }

    @Override
    public int getHttpStatus() {
        return status;
    }

    @Override
    public void run(HttpServletResponse exchange) throws IOException {
        exchange.setHeader(RESPONSE_HEADER_LOCATION, redirect);
        super.run(exchange);
    }

    @Override
    protected byte[] getBody() {
        return new byte[0];
    }
}
