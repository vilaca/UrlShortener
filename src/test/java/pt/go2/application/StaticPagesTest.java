package pt.go2.application;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import pt.go2.Mocks.HttpServletRequestMock;
import pt.go2.Mocks.HttpServletResponseMock;
import pt.go2.Mocks.ServletOutputStreamMock;
import pt.go2.fileio.Configuration;
import pt.go2.fileio.EmbeddedFiles;
import pt.go2.fileio.ErrorPages;
import pt.go2.storage.KeyValueStore;

public class StaticPagesTest {

    @Test
    public void test() throws IOException {

        final Configuration config;

        final KeyValueStore ks;
        final ErrorPages errors;
        final EmbeddedFiles res;

        config = new Configuration();
        ks = new KeyValueStore(config.getDbFolder());
        errors = new ErrorPages();
        res = new EmbeddedFiles(config);

        final StaticPages sp = new StaticPages(config, errors, ks, res);

        final HttpServletRequestMock request = new HttpServletRequestMock() {

            @Override
            public String getRequestURI() {
                return "/aabbcc";
            }

        };

        final HttpServletResponseMock exchange = new HttpServletResponseMock() {

            @Override
            public ServletOutputStream getOutputStream() {
                return new ServletOutputStreamMock();
            }
        };

        sp.handle(request, exchange);
    }
}
