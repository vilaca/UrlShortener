package pt.go2.storage;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";

    public enum Health {

        OK, PROCESSING, PHISHING, MALWARE;
    }

    private final byte[] inner;
    private final int hashcode;

    private volatile Health health;
    private Date updated;

    /**
     * Use create method instead
     *
     * @param bs
     * @param state
     */
    private Uri(final byte[] bs, final Health state) {
        inner = bs;
        hashcode = Arrays.hashCode(inner);
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

        try {
            return new Uri(normalized.getBytes("US-ASCII"), state);
        } catch (final UnsupportedEncodingException e) {
            LOGGER.error("ERROR decoding URI.", e);
            return null;
        }
    }

    @Override
    public int hashCode() {
        return hashcode;
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

        final byte[] otherInner = ((Uri) other).inner;

        if (this.inner.length != otherInner.length) {
            return false;
        }

        return Arrays.equals(this.inner, otherInner);
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
        this.updated = new Date();
    }

    public long lastChecked() {
        return updated == null ? 0 : updated.getTime();
    }

    /**
     * Get domain ( and TLD )
     *
     * @throws UnsupportedEncodingException
     */
    public String domain() throws UnsupportedEncodingException {

        String uri = new String(inner, "US-ASCII");

        // remove https/http
        int i = uri.indexOf("//");
        if (i != -1) {
            uri = uri.substring(i + "//".length());
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
