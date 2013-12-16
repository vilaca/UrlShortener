package pt.go2.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.go2.annotations.Injected;
import pt.go2.annotations.Page;
import pt.go2.response.AbstractResponse;
import pt.go2.response.SimpleResponse;

@Page(requireLogin = true, path = "api/user/changePassword/")
public class ChangePassword extends AbstractHandler {

	@Injected
	private UserMan users;

	@Override
	public void handle() throws IOException {

		final List<String> fields = users.getPasswordChangeFields();
		final Map<String, String> values = new HashMap<>(fields.size());

		if (!parseForm(values, fields, this.users)) {
			reply(ErrorMessages.Error.BAD_REQUEST);
			return;
		}

		if (!users.changePassword(values)) {
			reply(ErrorMessages.Error.FORBIDDEN_USER_ALREADY_EXISTS);
			return;
		}

		reply(new SimpleResponse(200, AbstractResponse.MIME_TEXT_PLAIN));
	}
}
