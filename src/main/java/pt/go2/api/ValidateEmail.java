package pt.go2.api;

import java.io.IOException;

import pt.go2.annotations.Injected;
import pt.go2.annotations.Page;
import pt.go2.application.Resources;

@Page(requireLogin = true, path = "api/user/validate/")
public class ValidateEmail extends AbstractHandler {

	@Injected
	private UserMan users;

	@Injected
	protected Resources vfs;

	@Override
	public void handle() throws IOException {

		final String[] tokens = tokenizeUrl();

		if (users.validate(tokens[0], tokens[1])) {
			reply(ErrorMessages.Error.USER_VALIDATED);
		} else {
			reply(ErrorMessages.Error.ERROR_VALIDATING_USER);
		}
	}
}
