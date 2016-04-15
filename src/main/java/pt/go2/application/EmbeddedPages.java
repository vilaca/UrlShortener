package pt.go2.application;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import pt.go2.fileio.Configuration;
import pt.go2.response.GenericResponse;
import pt.go2.response.GzipResponse;
import pt.go2.response.Response;

class EmbeddedPages {

    final Map<String, Response> pages = new HashMap<>();
    final Map<String, Response> zipped = new HashMap<>();

    public EmbeddedPages(Configuration config) throws IOException, URISyntaxException {

        final Class<EmbeddedPages> clazz = EmbeddedPages.class;

        byte[] index = Files.readAllBytes(new File(clazz.getResource("/index.html").toURI()).toPath());
        final byte[] ajax = Files.readAllBytes(new File(clazz.getResource("/ajax.js").toURI()).toPath());
        final byte[] robots = Files.readAllBytes(new File(clazz.getResource("/robots.txt").toURI()).toPath());
        final byte[] map = Files.readAllBytes(new File(clazz.getResource("/sitemap.xml").toURI()).toPath());
        final byte[] css = Files.readAllBytes(new File(clazz.getResource("/screen.css").toURI()).toPath());

        // zipped

        this.zipped.put("/", new GzipResponse(index, Response.MIME_TEXT_HTML));

        this.zipped.put("ajax.js", new GzipResponse(ajax, Response.MIME_APP_JAVASCRIPT));

        this.zipped.put("robots.txt", new GzipResponse(robots, Response.MIME_TEXT_PLAIN));

        this.zipped.put("sitemap.xml", new GzipResponse(map, Response.MIME_TEXT_XML));

        this.zipped.put("screen.css", new GzipResponse(css, Response.MIME_TEXT_CSS));

        // plain

        this.pages.put("/", GenericResponse.create(index, Response.MIME_TEXT_HTML));

        this.pages.put("ajax.js", GenericResponse.create(ajax, Response.MIME_APP_JAVASCRIPT));

        this.pages.put("robots.txt", GenericResponse.create(robots, Response.MIME_TEXT_PLAIN));

        this.pages.put("sitemap.xml", GenericResponse.create(map, Response.MIME_TEXT_XML));

        this.pages.put("screen.css", GenericResponse.create(css, Response.MIME_TEXT_CSS));

        // google verification for webmaster tools

        if (config.getGoogleVerification() != null && !config.getGoogleVerification().isEmpty())

        {
            this.zipped.put(config.getGoogleVerification(),
                    new GzipResponse(
                            ("google-site-verification: " + config.getGoogleVerification()).getBytes("US-ASCII"),
                            Response.MIME_TEXT_PLAIN));
        }

        // check if all pages created

        // TODO is this check required ??

        for (

        final String page : this.zipped.keySet())

        {

            final Response response = this.zipped.get(page);

            if (response == null) {

                throw new IOException("Failed to load page " + page);
            }
        }

    }

    public Response getFile(String filename, boolean compressed) {
        return compressed ? zipped.get(filename) : pages.get(filename);
    }

}
