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
 * 
 * @author vilaca
 * 
 */
public class Uri {

	private final static String[] SCHEMES = new String[] { "http", "https", "" };
	private final byte[] inner;
	private final int hashcode;

	public static Uri create(String str) {

		str = normalizeUrl(str);

		if (!new UrlValidator(SCHEMES).isValid(str)) {
			return null;
		}

		return new Uri(str);
	}

	/**
	 * User create method instead
	 * 
	 * @param str
	 */
	Uri(final String str) {
		inner = str.getBytes();
		hashcode = Arrays.hashCode(inner);
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
}
