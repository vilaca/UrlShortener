package pt.go2.application;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;

import pt.go2.fileio.Configuration;

// TODO implement builder pattern 

class EmbeddedPages {

    private static final Object[][] fileList = {

            { "/index.html", MimeTypeConstants.MIME_TEXT_HTML }, { "/ajax.js", MimeTypeConstants.MIME_APP_JAVASCRIPT },
            { "/robots.txt", MimeTypeConstants.MIME_TEXT_PLAIN }, { "/sitemap.xml", MimeTypeConstants.MIME_TEXT_XML },
            { "/screen.css", MimeTypeConstants.MIME_TEXT_CSS } };

    private static final Logger LOGGER = LogManager.getLogger();

    final Map<String, Response> pages = new HashMap<>();

    public EmbeddedPages(Configuration config) throws IOException, URISyntaxException {

        // google verification for webmaster tools

        if (config.getGoogleVerification() != null && !config.getGoogleVerification().isEmpty()) {
            this.pages.put(config.getGoogleVerification(),
                    ResponseFactory.create(HttpStatus.OK_200, MimeTypeConstants.MIME_TEXT_PLAIN, true,
                            ("google-site-verification: " + config.getGoogleVerification())
                                    .getBytes(StandardCharsets.US_ASCII)));
        }

        final Class<EmbeddedPages> clazz = EmbeddedPages.class;

        for (Object file[] : fileList) {
            final String filename = (String) file[0];
            final MimeTypeConstants mime = (MimeTypeConstants) file[1];

            final byte[] content = Files.readAllBytes(new File(clazz.getResource(filename).toURI()).toPath());
            final byte[] zipped;

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try (final GZIPOutputStream zip = new GZIPOutputStream(baos);) {

                zip.write(content);
                zip.flush();
                zip.close();

            } catch (final IOException e) {

                LOGGER.error(e);
            }

            zipped = baos.toByteArray();

            final Response response = ResponseFactory.create(200, mime, zipped, content);

            pages.put(filename.substring(1), response);
        }
    }

    public Response getFile(String filename) {
        return pages.get(filename);
    }

    public void setAlias(String alias, String filename) {

        final Response page = pages.get(filename);

        if (page == null) {
            
            // TODO throw initialization exception
            
            return;
        }

        pages.put(alias, page);
    }
}
