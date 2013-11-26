package pt.go2.application;

import java.io.BufferedWriter;
import java.io.IOException;

import pt.go2.fileio.Configuration;
import pt.go2.fileio.Statistics;
import pt.go2.response.JsonResponse;

import com.sun.net.httpserver.HttpExchange;

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

		final String output = statistics.getLast24Hours();

		reply(exchange, new JsonResponse(output), false);
	}

}
