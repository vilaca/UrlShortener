package pt.go2.response;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import com.sun.net.httpserver.HttpExchange;

public class StatsResponse extends AbstractResponse {

	private final static long boot = Calendar.getInstance().getTimeInMillis();

	@Override
	public int getHttpStatus() {
		return 200;
	}

	@Override
	public byte[] run(HttpExchange exchange) {

		final StringBuffer sb = new StringBuffer();

		sb.append("<html>");
		sb.append("<head><title>Go2.pt - Stats</title></head>");
		sb.append("<body>");

		sb.append("<h1>Uptime</h1>");
		sb.append("<p>" + uptime() + "</p>");

		sb.append("</body>");
		sb.append("</html>");

		return sb.toString().getBytes();

	}

	private String uptime() {

		final long now = Calendar.getInstance().getTimeInMillis();
		final long diff = now - boot;

		final long days = TimeUnit.MILLISECONDS.toDays(diff);
		final long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
		final long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);

		final StringBuffer sb = new StringBuffer();

		sb.append(days);
		sb.append(days == 1 ? " day " : " days ");
		sb.append(minutes);
		sb.append(days == 1 ? " minute " : " minutes ");
		sb.append(seconds);
		sb.append(seconds == 1 ? " seconds " : " seconds ");

		return sb.toString();
	}

	@Override
	public String getMimeType() {
		return MIME_TEXT_HTML;
	}

	@Override
	public boolean isCacheable()
	{
		return false;
	}

}
