package pt.go2.application;

import java.io.BufferedWriter;
import java.io.IOException;

import pt.go2.fileio.Configuration;
import pt.go2.response.JsonResponse;

import com.sun.net.httpserver.HttpExchange;

public class View extends AbstractHandler {

	public View(Configuration config, Resources vfs, BufferedWriter accessLog) {
		super(config, vfs, accessLog);
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		reply(exchange, new JsonResponse(vfs.browse()), false);
	}
}
