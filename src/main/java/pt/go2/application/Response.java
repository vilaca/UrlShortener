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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract class for server response
 */
public interface Response {

    /**
     * Http Status code for response
     *
     * @return
     */
    public int getHttpStatus();

    /**
     * Generate response
     *
     * @param exchange
     * @return
     * @throws IOException
     */
    public void run(HttpServletRequest request, HttpServletResponse response) throws IOException;

    public String getMimeType();

    public boolean isCacheable();
    
    /**
     * Read file from Jar file
     * 
     * @param filename
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static byte[] readFile(String filename) {

        final URL resource = EmbeddedPages.class.getResource(filename);

        if (resource == null) {
            throw new IllegalArgumentException(filename + " not present in jar file.");
        }
        
        try {
            return Files.readAllBytes(new File(resource.toURI()).toPath());
        } catch (IOException | URISyntaxException e) {
            throw new IllegalArgumentException("Cant open or read resource " + filename, e);
        }
    }
}