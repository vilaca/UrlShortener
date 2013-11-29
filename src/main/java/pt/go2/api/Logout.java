package pt.go2.api;

import java.io.IOException;

import pt.go2.response.AbstractResponse;

import com.sun.net.httpserver.HttpExchange;

import com.sun.net.httpserver.HttpHandler;

public class Logout implements HttpHandler {

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		exchange.getResponseHeaders().set(
				AbstractResponse.RESPONSE_HEADER_AUTHORIZATION, "");
	}
}
