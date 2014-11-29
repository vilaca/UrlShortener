package pt.go2.storage;

import java.io.UnsupportedEncodingException;

import org.apache.commons.validator.routines.UrlValidator;

/**
 */
public class Uri {

    public enum Health {

        OK, PROCESSING, PHISHING, MALWARE;
    }

    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";

    private volatile Health health;

    private final String uri;

    /**
     * Use create method instead
     *
     * @param bs
     * @param state
     */
    private Uri(String uri, final Health state) {
        this.uri = uri;
        this.health = state;
    }

    public static Uri create(final String str, final boolean validate) {
        return create(str, validate, Health.OK);
    }

    public static Uri create(String str, final boolean validate, Health state) {

        final String normalized = normalizeUrl(str);

        if (validate && !new UrlValidator(new String[] { "http", "https", "" }).isValid(normalized)) {
            return null;
        }

        return new Uri(normalized, state);
    }

    @Override
    public int hashCode() {
        return this.uri.hashCode();
    }

    @Override
    public boolean equals(Object other) {

        if (other == null) {
            return false;
        }

        if (other == this) {
            return true;
        }

        if (other.getClass() != getClass()) {
            return false;
        }

        final String otherUri = ((Uri) other).uri;

        return this.uri.length() == otherUri.length() && this.uri.equals(otherUri);
    }

    /**
     * Strategy to identify repeated URLs more easily. JavaScript also does
     * something similar but can't trust input to be correct.
     *
     * @param url
     * @return
     */
    private static String normalizeUrl(String raw) {

        String input = raw.trim();

        final int idxDomain;

        // normalize Url ending

        if (input.endsWith("/")) {
            input = input.substring(0, input.length() - 1);
        }

        if (input.startsWith(HTTPS)) {

            idxDomain = input.substring(HTTPS.length()).indexOf("/") + HTTPS.length();

        } else if (input.startsWith(HTTP)) {

            idxDomain = input.substring(HTTP.length()).indexOf("/") + HTTP.length();

        } else {
            input = HTTP + input;
            idxDomain = input.substring(HTTP.length()).indexOf("/") + HTTP.length();
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
    }

    /**
     * Get domain ( and TLD )
     *
     * @throws UnsupportedEncodingException
     */
    public String domain() {

        String domain = this.uri;

        // remove https/http
        int i = domain.indexOf("//");
        if (i != -1) {
            domain = domain.substring(i + "//".length());
        }

        // remove file path
        i = domain.indexOf("/", i);
        if (i != -1) {
            domain = domain.substring(0, i);
        }
        // remove port
        i = domain.indexOf(":");
        if (i != -1) {
            domain = domain.substring(0, i);
        }

        // remove subdomain
        i = domain.lastIndexOf(".", domain.lastIndexOf(".") - 1);
        if (i != -1) {
            domain = domain.substring(i + 1);
        }

        return domain;
    }

    @Override
    public String toString() {
        return this.uri;
    }
}
