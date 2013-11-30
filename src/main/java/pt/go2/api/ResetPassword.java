package pt.go2.api;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.sun.net.httpserver.HttpExchange;

import pt.go2.application.AbstractFormHandler;
import pt.go2.application.Resources;
import pt.go2.application.UserMan;
import pt.go2.daemon.MailQueue;
import pt.go2.fileio.Configuration;
import pt.go2.response.AbstractResponse;
import pt.go2.response.SimpleResponse;

public class ResetPassword extends AbstractFormHandler {

	private static final String PARAGRAPH_END = "\r\n\r\n";
	private final Random rnd = new Random();
	private final MailQueue mail;
	private final UserMan users;

	public ResetPassword(Configuration config, Resources vfs,
			BufferedWriter accessLog, final UserMan users, final MailQueue mail) {
		super(config, vfs, accessLog);

		this.users = users;
		this.mail = mail;
	}

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
