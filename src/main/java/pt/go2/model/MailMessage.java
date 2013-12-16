package pt.go2.model;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailMessage {

	private final String to;
	private final String subject;
	private final String text;

	public MailMessage(final String to, final String subject, final String text) {
		this.to = to;
		this.subject = subject;
		this.text = text;
	}

	public MimeMessage createMessage(final Session session, final String from) {

		final MimeMessage message = new MimeMessage(session);

		try {

			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO,
					new InternetAddress(to));
			message.setSubject(subject);
			message.setText(text);
			return message;

		} catch (MessagingException e) {
			return null;
		}
	}
}
