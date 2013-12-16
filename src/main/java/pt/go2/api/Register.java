package pt.go2.api;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import pt.go2.annotations.Injected;
import pt.go2.annotations.Page;
import pt.go2.response.AbstractResponse;
import pt.go2.response.SimpleResponse;

@Page(requireLogin = true, path = "api/user/register/")
public class Register extends AbstractHandler {

	private static final String PARAGRAPH_END = "\r\n\r\n";

	@Injected
	private MailQueue mail;

	@Injected
	private UserMan users;

	@Override
	public void handle() throws IOException {

		final List<String> fields = UserMan.getUserFields();
		final Map<String, String> values = new HashMap<>(fields.size());

		if (!parseForm(values, fields, this.users)) {
			reply(ErrorMessages.Error.BAD_REQUEST);
			return;
		}

		final String username = values.get(UserMan.USER_NAME);

		if (users.exist(username)) {
			reply(ErrorMessages.Error.FORBIDDEN_USER_ALREADY_EXISTS);
			return;
		}

		final String token = UUID.randomUUID().toString();
		final String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(Calendar.getInstance().getTime());

		values.put(UserMan.USER_VALIDATION_TOKEN, token);
		values.put(UserMan.USER_CREATION_DATE, date);

		if (!users.save(values)) {
			reply(ErrorMessages.Error.ERROR_CREATING_USER);
			return;
		}

		final StringBuilder sb = new StringBuilder();

		sb.append("Thank you for registering at "
				+ config.MAIL_SITE_NAME
				+ ". Before we can activate your account one last step must be taken to complete your registration.");
		sb.append(PARAGRAPH_END);

		sb.append("To complete your registration, please visit the following link: ");
		sb.append(config.MAIL_LINK_URL + username + "/" + token);
		sb.append(PARAGRAPH_END);

		sb.append("If you did not register at " + config.MAIL_SITE_NAME
				+ ", please ignore this message.");
		sb.append(PARAGRAPH_END);

		final String email = values.get(UserMan.USER_EMAIL);
		mail.addMessage(email,
				"Activate your membership. Please verify your Email Address.",
				sb.toString());

		// TODO what to do after register ?
		reply(new SimpleResponse(200, AbstractResponse.MIME_TEXT_PLAIN));
	}
}
