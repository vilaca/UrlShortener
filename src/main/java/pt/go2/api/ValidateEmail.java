package pt.go2.api;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import pt.go2.annotations.Page;
import pt.go2.annotations.Injected;
import pt.go2.application.Resources;

@Page(requireLogin = true, path = "api/user/validate/")
public class ValidateEmail extends AbstractHandler {

	@Injected
	private UserMan users;

	@Injected
	protected Resources vfs;
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {

		final String path = exchange.getHttpContext().getPath();

		final String token = exchange.getRequestURI().getPath()
				.substring(path.length());

		final String[] tokens = token.split("/");

		if (users.validate(tokens[0], tokens[1])) {
			reply(exchange, vfs.get(Resources.Error.USER_VALIDATED), false);
		} else {
			reply(exchange, vfs.get(Resources.Error.ERROR_VALIDATING_USER), false);
		}
	}
}
