package pt.go2.api;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.go2.application.AbstractFormHandler;
import pt.go2.application.Resources;
import pt.go2.application.UserMan;
import pt.go2.fileio.Configuration;
import pt.go2.response.AbstractResponse;

import com.sun.net.httpserver.HttpExchange;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class Login extends AbstractFormHandler {

	final UserMan users;

	public Login(Configuration config, Resources vfs, BufferedWriter accessLog,
			final UserMan users) {
		super(config, vfs, accessLog);

		this.users = users;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		final List<String> fields = UserMan.getUserFields();
		final Map<String, String> values = new HashMap<>(fields.size());

		if (!parseForm(exchange, values, fields, this.users)) {
			return;
		}

		final String username = values.get(UserMan.USER_NAME);
		final String password = values.get(UserMan.USER_PASSWORD);

		if (!users.login(username, password)) {
			reply(exchange, vfs.get(Resources.Error.FORBIDDEN), false);
			return;
		}

		final String token = "Basic "
				+ new String(Base64.encode((username + ":" + password)
						.getBytes()));

		exchange.getResponseHeaders().set(
				AbstractResponse.RESPONSE_HEADER_AUTHORIZATION, token);

		reply(exchange, vfs.get(Resources.Error.USER_LOGIN_SUCESSFUL), false);

	}
}
