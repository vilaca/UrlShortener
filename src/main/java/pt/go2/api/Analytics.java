package pt.go2.api;

import java.io.BufferedWriter;
import java.io.IOException;

import pt.go2.annotations.Page;
import pt.go2.application.AbstractHandler;
import pt.go2.application.Resources;
import pt.go2.fileio.Configuration;
import pt.go2.fileio.Statistics;
import pt.go2.response.JsonResponse;

import com.sun.net.httpserver.HttpExchange;

@Page(requireLogin = true, path = "api/statistics/")
public class Analytics extends AbstractHandler {

	private final Statistics statistics;

	public Analytics(Configuration config, Resources vfs,
			Statistics statistics, BufferedWriter accessLog) {

		super(config, vfs, accessLog);
		this.statistics = statistics;
	}

	/**
	 * Server statistics
	 * 
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		reply(exchange, new JsonResponse(statistics.getLast24Hours()), false);
	}
}
