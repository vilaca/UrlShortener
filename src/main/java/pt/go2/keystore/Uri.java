package pt.go2.keystore;

import java.util.Arrays;

import org.apache.commons.validator.routines.UrlValidator;

/**
 * Immutable ASCII string
 * 
 * More memory efficient than String by using byte instead of char.
 * 
 * Other future optimizations are possible.
 * 
 * MUST OVERRIDE BOTH hashCode() and equals(Object). hashCode() value must be
 * calculated in c'tor for faster lookups in Map
 */
public class Uri {

	enum State {
		OK, OFFLINE, FORBIDEN_PHISHING, FORBIDDEN_MALWARE
	}

	private final static String[] SCHEMES = new String[] { "http", "https", "" };
	private final byte[] inner;
	private final int hashcode;

	private State state;

	public static Uri create(final String str, final boolean validate) {
		return create ( str, validate, State.OK );
	}

	public static Uri create(String str, final boolean validate, State state) {

		str = normalizeUrl(str);

		if (validate && !new UrlValidator(SCHEMES).isValid(str)) {
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
	private Uri(final String str, final State state) {
		inner = str.getBytes();
		hashcode = Arrays.hashCode(inner);
		this.state = state;
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null)
			return false;

		if (obj == this)
			return true;

		if (obj.getClass() != getClass())
			return false;

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

			idxDomain = input.substring("https://".length()).indexOf("/")
					+ "https://".length();

		} else if (input.startsWith("http://")) {

			idxDomain = input.substring("http://".length()).indexOf("/")
					+ "http://".length();

		} else {
			input = "http://" + input;
			idxDomain = input.substring("http://".length()).indexOf("/")
					+ "http://".length();
		}

		// make sure domain and TLD are lower case

		input = input.substring(0, idxDomain).toLowerCase()
				+ input.substring(idxDomain);

		// add http:// scheme if needed

		return input;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}
}
