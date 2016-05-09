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
package pt.go2.external;

import java.util.Collections;
import java.util.List;

import org.eclipse.jetty.http.HttpStatus;

public class HttpClientResponse {

    private final int status;
    private final List<String> records;

    public HttpClientResponse(int status) {
        this.status = status;
        this.records = Collections.emptyList();
    }

    public HttpClientResponse(List<String> lines) {
        this.status = HttpStatus.OK_200;
        this.records = Collections.unmodifiableList(lines);
    }

    public int status() {
        return status;
    }

    public List<String> records() {
        return records;
    }
}
