package pt.go2.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.go2.annotations.Injected;
import pt.go2.annotations.Page;
import pt.go2.application.AbstractFormHandler;
import pt.go2.application.Resources;
import pt.go2.application.UserMan;
import pt.go2.response.AbstractResponse;
import pt.go2.response.SimpleResponse;

import com.sun.net.httpserver.HttpExchange;

@Page(requireLogin = true, path = "api/user/changePassword/")
public class ChangePassword extends AbstractFormHandler {

	@Injected
	private Resources vfs;
	
	@Injected
	private UserMan users;

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
