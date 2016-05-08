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

enum HeaderConstants {

    // Request headers
    REQUEST_HEADER_ACCEPT_ENCODING("Accept-encoding"),
    REQUEST_HEADER_HOST("Host"),
    REQUEST_HEADER_REFERER("Referer"),
    REQUEST_HEADER_USER_AGENT("User-Agent"),

    // Response headers
    RESPONSE_HEADER_CACHE_CONTROL("Cache-Control"),
    RESPONSE_HEADER_CONTENT_ENCODING("Content-Encoding"),
    RESPONSE_HEADER_CONTENT_TYPE("Content-Type"),
    RESPONSE_HEADER_EXPIRES("Expires"),
    RESPONSE_HEADER_LOCATION("Location"),
    RESPONSE_HEADER_SERVER("Server");
    
    private final String value;
    
    HeaderConstants(String value)
    {
        this.value = value;
    }
    
    @Override
    public String toString()
    {
        return value;
    }
}
