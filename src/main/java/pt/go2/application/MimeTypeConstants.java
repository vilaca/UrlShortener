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

enum MimeTypeConstants {

    // mime types
    MIME_APP_JAVASCRIPT("application/javascript"),
    MIME_APP_JSON("application/json"),

    MIME_IMG_JPEG("image/jpeg"),
    MIME_IMG_GIF("image/gif"),
    MIME_IMG_PNG("image/png"),

    MIME_TEXT_CSS("text/css"),
    MIME_TEXT_HTML("text/html"),
    MIME_TEXT_PLAIN("text/plain"),
    MIME_TEXT_XML("text/xml");
    
    private final String value;
    
    MimeTypeConstants(String value)
    {
        this.value = value;
    }
    
    @Override
    public String toString()
    {
        return value;
    }
}
