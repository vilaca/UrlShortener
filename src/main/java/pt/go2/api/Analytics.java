package pt.go2.api;

import java.io.IOException;

import pt.go2.annotations.Injected;
import pt.go2.annotations.Page;
import pt.go2.response.JsonResponse;

import com.sun.net.httpserver.HttpExchange;

@Page(requireLogin = true, path = "api/statistics/")
public class Analytics extends AbstractHandler {

	@Injected
	private Statistics statistics;
	
	/**
	 * Server statistics
	 * 
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		reply(exchange, new JsonResponse(statistics.getLast24Hours()), false);
	}
}
