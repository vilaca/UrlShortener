package pt.go2.api;

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import pt.go2.daemon.WatchDogTask;
import pt.go2.fileio.Configuration;
import pt.go2.model.MailMessage;

public class MailQueue implements WatchDogTask {

	private Queue<MailMessage> queue = new ConcurrentLinkedQueue<>();
	private final Properties properties = new Properties();
	private volatile Date lastDownload;
	private final Session session;
	private final String email;

	public MailQueue(final Configuration config) {

		// TODO load properties from file?

		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.host", config.SMTP_SERVER_HOST);
		properties.put("mail.smtp.port", config.SMTP_SERVER_PORT);
		properties.put("mail.smtp.socketFactory.port", config.SMTP_SERVER_PORT);
		properties.put("mail.smtp.socketFactory.fallback", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");

		session = Session.getDefaultInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(config.SMTP_SERVER_USER,
						config.SMTP_SERVER_PASSWORD);
			}
		});

		email = config.SMTP_OUTBOUND_EMAIL;
	}

	@Override
	public void refresh() {

		MailMessage qm = queue.poll();

		if (qm == null) {
			return;
		}

		do {
			try {
				final MimeMessage message = qm.createMessage(session, email);

				if (message != null) {
					Transport.send(message);
					lastDownload = Calendar.getInstance().getTime();
				}

			} catch (MessagingException mex) {
				mex.printStackTrace();
			}

			qm = queue.poll();

		} while (qm != null);
	}

	@Override
	public Date lastRun() {
		return lastDownload;
	}

	@Override
	public long interval() {
		return 1;
	}

	public void addMessage(String to, String subject, String text) {

		final MailMessage qm = new MailMessage(to, subject, text);

		synchronized (this) {
			queue.add(qm);
		}
	}
}
