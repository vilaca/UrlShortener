package pt.go2.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import pt.go2.annotations.Injected;
import pt.go2.annotations.Page;
import pt.go2.application.Resources;
import pt.go2.response.AbstractResponse;
import pt.go2.response.SimpleResponse;

import com.sun.net.httpserver.HttpExchange;

@Page(requireLogin = true, path = "api/user/resetPassword/")
public class ResetPassword extends AbstractFormHandler {

	private static final String PARAGRAPH_END = "\r\n\r\n";
	private final Random rnd = new Random();

	@Injected
	private MailQueue mail;

	@Injected
	private UserMan users;

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		final List<String> fields = UserMan.getUserFields();
		final Map<String, String> values = new HashMap<>(fields.size());

		if (!parseForm(exchange, values, fields, this.users)) {
			reply(exchange, vfs.get(Resources.Error.BAD_REQUEST), false);
			return;
		}

		final byte[] validchars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-+!"
				.getBytes();

		final byte[] newb = new byte[12];

		for (int i = 0; i < newb.length; i++) {
			newb[i] = (byte) rnd.nextInt(validchars.length);
		}

		final String username = values.get(UserMan.USER_NAME);
		final String newPassword = new String(newb);

		if (users.changePassword(username, newPassword)) {
			reply(exchange, vfs.get(Resources.Error.ERROR_VALIDATING_USER),
					false);
			return;
		}

		final StringBuilder sb = new StringBuilder();

		sb.append("Dear " + username + ".");
		sb.append(PARAGRAPH_END);

		sb.append("Your new password is: " + newPassword);
		sb.append(PARAGRAPH_END);

		final String email = values.get(UserMan.USER_EMAIL);
		mail.addMessage(email, "New password for " + username + ".",
				sb.toString());

		reply(exchange, new SimpleResponse(200,
				AbstractResponse.MIME_TEXT_PLAIN), false);
	}
}
