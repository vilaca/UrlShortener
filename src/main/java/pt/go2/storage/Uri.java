package pt.go2.storage;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.validator.routines.UrlValidator;

/**
 * Immutable ASCII string
 * 
 * More memory efficient than String by using byte instead of char.
 * 
 * Other future optimizations are possible.
 * 
 * MUST OVERRIDE BOTH hashCode() and equals(Object). hashCode() value calculated
 * in c'tor for faster lookups in Map
 */
public class Uri {

	public enum Health {

		OK, PROCESSING, PHISHING, MALWARE;
	}

	private final byte[] inner;
	private final int hashcode;

	private volatile Health health;
	private Date updated;

	public static Uri create(final String str, final boolean validate) {
		return create(str, validate, Health.OK);
	}

	public static Uri create(String str, final boolean validate, Health state) {

		str = normalizeUrl(str);

		if (validate && !new UrlValidator(new String[] { "http", "https", "" }).isValid(str)) {
			return null;
		}

		return new Uri(str, state);
	}

	/**
	 * User create method instead
	 * 
	 * @param str
	 * @param state
	 */
	private Uri(final String str, final Health state) {
		inner = str.getBytes();
		hashcode = Arrays.hashCode(inner);
		this.health = state;
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj.getClass() != getClass()) {
			return false;
		}

		byte[] inner = ((Uri) obj).inner;

		if (this.inner.length != inner.length)
			return false;

		return Arrays.equals(this.inner, inner);
	}

	@Override
	public String toString() {
		return new String(inner);
	}

	/**
	 * Strategy to identify repeated URLs more easily. JavaScript also does
	 * something similar but can't trust input to be correct.
	 * 
	 * @param url
	 * @return
	 */
	private static String normalizeUrl(String input) {

		input = input.trim();

		final int idxDomain;

		// normalize Url ending

		if (input.endsWith("/")) {
			input = input.substring(0, input.length() - 1);
		}

		if (input.startsWith("https://")) {

			idxDomain = input.substring("https://".length()).indexOf("/") + "https://".length();

		} else if (input.startsWith("http://")) {

			idxDomain = input.substring("http://".length()).indexOf("/") + "http://".length();

		} else {
			input = "http://" + input;
			idxDomain = input.substring("http://".length()).indexOf("/") + "http://".length();
		}

		// make sure domain and TLD are lower case

		input = input.substring(0, idxDomain).toLowerCase() + input.substring(idxDomain);

		return input;
	}

	public Health health() {
		return health;
	}

	public void setHealth(final Health h) {
		this.health = h;
		this.updated = new Date();
	}

	public long lastChecked() {
		return updated == null ? 0 : updated.getTime();
	}

	/**
	 * Get domain ( and TLD )
	 */
	public String domain() {

		String uri = toString();

		// remove https/http
		int i = uri.indexOf("//");
		if (i != -1) {
			uri = uri.substring(i + 2);
		}

		// remove file path
		i = uri.indexOf("/", i);
		if (i != -1) {
			uri = uri.substring(0, i);
		}
		// remove port
		i = uri.indexOf(":");
		if (i != -1) {
			uri = uri.substring(0, i);
		}

		// remove subdomain
		i = uri.lastIndexOf(".", uri.lastIndexOf(".") - 1);
		if (i != -1) {
			uri = uri.substring(i + 1);
		}

		return uri;
	}
}
