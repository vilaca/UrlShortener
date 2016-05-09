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
package pt.go2.fileio;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Helper class for logging user access file
 *
 */
public class AccessLogger {

    private static final Logger LOGGER = LogManager.getLogger("accesslogger");
    
    /**
     * Access log output
     *
     * @param status
     *
     * @param request
     * @param referer 
     * @param agent 
     * @param exchange
     * @param exchange
     *
     * @param params
     * @param response
     * @return
     */
    public void log(int status, HttpServletRequest request, final int size, String referer, String agent) {

        final StringBuilder sb = new StringBuilder();

        sb.append(request.getRemoteAddr());
        sb.append(" - - [");
        sb.append(new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z").format(new Date()));
        sb.append("] \"");
        sb.append(request.getMethod());
        sb.append(" ");
        sb.append(request.getRequestURI());
        sb.append(" ");
        sb.append(request.getProtocol());
        sb.append("\" ");
        sb.append(status);
        sb.append(" ");
        sb.append(size);
        sb.append(" \"");

        sb.append(referer == null ? "-" : referer);

        sb.append("\" \"" + agent + "\"");

        LOGGER.trace(sb.toString());
    }
}
