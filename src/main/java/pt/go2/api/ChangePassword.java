package pt.go2.api;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import pt.go2.annotations.Page;
import pt.go2.application.AbstractFormHandler;
import pt.go2.application.Resources;
import pt.go2.application.UserMan;
import pt.go2.fileio.Configuration;
import pt.go2.response.AbstractResponse;
import pt.go2.response.SimpleResponse;

@Page(requireLogin = true, path = "api/user/changePassword/")
public class ChangePassword extends AbstractFormHandler {

	private final UserMan users;

	public ChangePassword(Configuration config, Resources vfs,
			BufferedWriter accessLog, final UserMan users) {
		super(config, vfs, accessLog);

		this.users = users;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		final List<String> fields = users.getPasswordChangeFields();
		final Map<String, String> values = new HashMap<>(fields.size());

		if (!parseForm(exchange, values, fields, this.users)) {
			reply(exchange, vfs.get(Resources.Error.BAD_REQUEST), false);
			return;
		}

		if (!users.changePassword(values)) {
			reply(exchange,
					vfs.get(Resources.Error.FORBIDDEN_USER_ALREADY_EXISTS),
					false);
			return;
		}

		reply(exchange, new SimpleResponse(200,
				AbstractResponse.MIME_TEXT_PLAIN), false);

	}
}
