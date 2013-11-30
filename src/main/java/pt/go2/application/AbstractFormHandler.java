package pt.go2.application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import pt.go2.fileio.Configuration;

public abstract class AbstractFormHandler extends AbstractHandler {

	public AbstractFormHandler(Configuration config, Resources vfs,
			BufferedWriter accessLog) {
		super(config, vfs, accessLog);
	}

	protected boolean parseForm(HttpExchange exchange,
			final Map<String, String> values, List<String> fields,
			final UserMan users) throws IOException {

		int remaining = fields.size();

		try (final InputStream is = exchange.getRequestBody();
				final InputStreamReader sr = new InputStreamReader(is);
				final BufferedReader br = new BufferedReader(sr);) {

			// read body content

			do {

				final String line = br.readLine();

				if (line == null) {
					reply(exchange, vfs.get(Resources.Error.BAD_REQUEST), false);
					return false;
				}

				int idx = line.indexOf('=');

				if (idx == -1) {
					reply(exchange, vfs.get(Resources.Error.BAD_REQUEST), false);
					return false;
				}

				final String field = line.substring(0, idx);

				idx = fields.indexOf(field);

				if (idx == -1) {
					reply(exchange, vfs.get(Resources.Error.BAD_REQUEST), false);
					return false;
				}

				final String value = line.substring(idx + 1);

				if (users!=null && !users.validateUserProperty(field, value)) {
					reply(exchange, vfs.get(Resources.Error.BAD_REQUEST), false);
					return false;
				}

				final String prev = values.put(field, value);

				if (prev != null) {
					reply(exchange, vfs.get(Resources.Error.BAD_REQUEST), false);
					return false;
				}

				remaining--;

			} while (remaining > 0);

			return true;
		}
	}
}
