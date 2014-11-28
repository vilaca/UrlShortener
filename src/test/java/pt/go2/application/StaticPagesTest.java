package pt.go2.application;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import pt.go2.fileio.Configuration;
import pt.go2.fileio.EmbeddedFiles;
import pt.go2.fileio.ErrorPages;
import pt.go2.fileio.RestoreItem;
import pt.go2.mocks.HttpServletRequestMock;
import pt.go2.mocks.HttpServletResponseMock;
import pt.go2.response.AbstractResponse;
import pt.go2.storage.KeyValueStore;

public class StaticPagesTest {

    @Test
    public void test() throws IOException {

        final String redirected = "http://redirected.com";

        final List<RestoreItem> uris = Arrays.asList(new RestoreItem("aabbcc", redirected));

        final Configuration config = new Configuration();

        final KeyValueStore ks = new KeyValueStore(uris, config.getDbFolder());
        final ErrorPages errors = new ErrorPages();
        final EmbeddedFiles res = new EmbeddedFiles(config);

        final HttpServletRequestMock request = new HttpServletRequestMock() {

            @Override
            public String getRequestURI() {
                return "/aabbcc";
            }

        };

        final HttpServletResponseMock response = new HttpServletResponseMock();

        final StaticPages sp = new StaticPages(config, errors, ks, res);

        sp.handle(request, response);

        Assert.assertEquals(config.getRedirect(), response.getStatus());
        Assert.assertEquals(redirected, response.getHeader(AbstractResponse.RESPONSE_HEADER_LOCATION));

    }
}
