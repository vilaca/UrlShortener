package pt.go2.api;

import java.io.IOException;

import pt.go2.annotations.Injected;
import pt.go2.annotations.Page;
import pt.go2.application.AbstractHandler;
import pt.go2.application.Resources;
import pt.go2.response.JsonResponse;

import com.sun.net.httpserver.HttpExchange;

@Page(requireLogin = true, path = "api/view/")
public class View extends AbstractHandler {

	@Injected
	protected Resources vfs;
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		reply(exchange, new JsonResponse(vfs.browse()), false);
	}
}
