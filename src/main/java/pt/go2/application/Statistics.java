package pt.go2.application;

import java.io.BufferedWriter;
import java.io.IOException;

import pt.go2.fileio.Configuration;
import pt.go2.response.NormalResponse;

import com.sun.net.httpserver.HttpExchange;

public class Statistics extends AbstractHandler {

	public Statistics(Configuration config, VirtualFileSystem vfs,
			BufferedWriter accessLog) {
		super(config, vfs, accessLog);
	}

	/**
	 * Server statistics
	 * 
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {

		final StringBuffer sb = new StringBuffer();

		sb.append("<html>");
		sb.append("<head><title>Go2.pt - Stats</title></head>");
		sb.append("<body>");

		sb.append("<h1>Uptime</h1>");
		sb.append("<p>" + "uptime()" + "</p>");

		sb.append("</body>");
		sb.append("</html>");

		reply(exchange, new NormalResponse(sb.toString().getBytes()), false);
	}

}
