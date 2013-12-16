package pt.go2.api;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.go2.annotations.Injected;
import pt.go2.annotations.Page;
import pt.go2.application.Resources;
import pt.go2.keystore.HashKey;
import pt.go2.keystore.KeyValueStore;
import pt.go2.keystore.Uri;
import pt.go2.response.AbstractResponse;
import pt.go2.response.RedirectResponse;

/**
 * Handles server requests
 */
@Page(requireLogin = false, path = "/")
class StaticPages extends AbstractHandler {

	static final Logger logger = LogManager.getLogger(StaticPages.class);

	@Injected
	private Resources vfs;

	@Injected
	private KeyValueStore ks;

	/**
	 * Handle request, parse URI filename from request into page resource
	 * 
	 * @param
	 * 
	 * @exception IOException
	 */
	@Override
	public void handle() throws IOException {

		String requested = getRawPath();

		if (requested.length() == 7) {

			requested = requested.substring(1);

			statistics(requested);

			final Uri uri = ks.get(new HashKey(requested.substring(1)));

			if (uri == null) {
				reply(ErrorMessages.Error.PAGE_NOT_FOUND);
				return;
			}

			if (vfs.isBanned(uri)) {
				logger.warn("banned: " + uri);
				reply(ErrorMessages.Error.FORBIDDEN_PHISHING);
				return;
			}

			reply(new RedirectResponse(uri.toString(), 301));
			return;
		}

		AbstractResponse response = vfs.get(requested);

		if (response != null) {
			reply(response);

		} else {
			reply(ErrorMessages.Error.PAGE_NOT_FOUND);
		}
	}

	@Override
	protected boolean getCache() {
		return true;
	}
}
