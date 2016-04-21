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

class EmbeddedPages {

    private static final String[][] fileList = { 
    
            { "/index.html", HeaderConstants.MIME_TEXT_HTML.toString() },
            { "/ajax.js", HeaderConstants.MIME_APP_JAVASCRIPT.toString() },
            { "/robots.txt", HeaderConstants.MIME_TEXT_PLAIN.toString() },
            { "/sitemap.xml", HeaderConstants.MIME_TEXT_XML.toString() },
            { "/screen.css", HeaderConstants.MIME_TEXT_CSS.toString() }
    };

    private static final Logger LOGGER = LogManager.getLogger();
    
    final Map<String, Response> pages = new HashMap<>();

    public EmbeddedPages(Configuration config) throws IOException, URISyntaxException {

        // google verification for webmaster tools

        if (config.getGoogleVerification() != null && !config.getGoogleVerification().isEmpty())
        {
            this.pages.put(
                    config.getGoogleVerification(), ResponseFactory.create(
                                HttpStatus.OK_200,
                                HeaderConstants.MIME_TEXT_PLAIN,
                                true,
                                ("google-site-verification: " + config.getGoogleVerification()).getBytes(StandardCharsets.US_ASCII)
                            )
                    );
        }

        final Class<EmbeddedPages> clazz = EmbeddedPages.class;
        
        for ( String file[]: fileList)
        {
            final byte[] content = Files.readAllBytes(new File(clazz.getResource(file[0]).toURI()).toPath());
            final byte[] zipped;

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            try ( final GZIPOutputStream zip = new GZIPOutputStream(baos);) {

                zip.write(content);
                zip.flush();
                zip.close();

            } catch (final IOException e) {

                LOGGER.error(e);
            }

            zipped = baos.toByteArray();
            
            pages.put(file[0], ResponseFactory.create(200, file[0], zipped, content) ); 
        }
    }

    public Response getFile(String filename) {
        return pages.get(filename);
    }
}
