/*
    Copyright (C) 2016 João Vilaça

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
 */
package pt.go2.application;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.eclipse.jetty.http.HttpStatus;

/**
 * Immutable class for static pages stored in the .jar file.
 * 
 * Includes HTML, CSS, JS and more.
 * 
 * @author Joao Vilaca
 */
final class EmbeddedPages {

    final Map<String, Response> pages;

    /**
     * Get static page. Page must be present in Jar file.
     * 
     * @param filename
     * @return
     */
    public Response getFile(String filename) {
        return pages.get(filename);
    }

    /**
     * Private constructor, use builder class.
     */
    private EmbeddedPages(final Map<String, Response> pages) {
        this.pages = pages;
    }

    /**
     * Creates EmbeddedPages class.
     * 
     * @author vilaca
     */
    static class Builder {

        final Map<String, Response> files;

        public Builder() {

            files = new HashMap<>();
        }

        /**
         * Add file to be embedded. File must be present in Jar.
         * 
         * @param filename
         * @param mime
         * 
         * @return
         * @throws URISyntaxException
         * @throws IOException
         */
        public Builder add(String filename, MimeTypeConstants mime) throws IOException, URISyntaxException {

            final byte[] content = readFile(filename);
            final byte[] zipped = compress(content);

            final Response response = ResponseFactory.create(HttpStatus.OK_200, mime, zipped, content);

            files.put(filename, response);
            return this;
        }

        /**
         * Add file to be embedded. File must be present in Jar.
         * 
         * @param filename
         * @param mime
         * 
         * @return
         */
        public Builder add(String filename, byte[] content, MimeTypeConstants mime) {

            final Response response = ResponseFactory.create(HttpStatus.OK_200, mime, true, content);
            files.put(filename, response);
            return this;
        }

        /**
         * Set alias for file. File must have been added with add() method or
         * create method will fail.
         * 
         * @param alias
         * @param filename
         * 
         * @return
         */
        public Builder setAlias(String alias, String filename) {

            final Response file = files.get(filename);

            if (file != null) {

                files.put(alias, file);
                return this;
            }

            throw new IllegalArgumentException("Cant create alias for " + filename);
        }

        /**
         * Create Embedded pages object instance
         * 
         * @return
         */
        public EmbeddedPages create() {
            return new EmbeddedPages(Collections.unmodifiableMap(files));
        }

        /**
         * Read file from Jar file
         * 
         * @param filename
         * @return
         * @throws IOException
         * @throws URISyntaxException
         */
        private byte[] readFile(String filename) throws IOException, URISyntaxException {

            final URL resource = EmbeddedPages.class.getResource(filename);

            if (resource != null) {
                return Files.readAllBytes(new File(resource.toURI()).toPath());
            }
            
            throw new IllegalArgumentException(filename + " not present in jar file.");
        }

        /**
         * Compress file content using GZIP
         * 
         * @param content
         * @return
         * @throws IOException 
         */
        private byte[] compress(byte[] content) throws IOException {
           
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try (final GZIPOutputStream zip = new GZIPOutputStream(baos);) {

                zip.write(content);
                zip.flush();
                zip.close();
            } 

            return baos.toByteArray();
        }
    }
}
