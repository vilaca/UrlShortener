package pt.go2.api;

import java.io.IOException;

import pt.go2.annotations.Injected;
import pt.go2.annotations.Page;
import pt.go2.response.JsonResponse;

@Page(requireLogin = true, path = "api/statistics/")
public class Analytics extends AbstractHandler {

	@Injected
	private Statistics statistics;

	/**
	 * Server statistics
	 * 
	 */
	@Override
	public void handle() throws IOException {
		reply(new JsonResponse(statistics.getLast24Hours()));
	}
}
