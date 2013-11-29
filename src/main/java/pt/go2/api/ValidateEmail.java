package pt.go2.api;

import java.io.BufferedWriter;
import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import pt.go2.application.AbstractHandler;
import pt.go2.application.Resources;
import pt.go2.application.UserMan;
import pt.go2.fileio.Configuration;

public class ValidateEmail extends AbstractHandler {

	final UserMan users;

	public ValidateEmail(final Configuration config, final Resources vfs,
			final BufferedWriter accessLog, final UserMan users) {

		super(config, vfs, accessLog);

		this.users = users;
	}

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
