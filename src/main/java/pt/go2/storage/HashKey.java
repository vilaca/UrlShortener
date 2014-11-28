package pt.go2.storage;

/**
 * Each Hashkey unequivocally identifies an Url
 */
public class HashKey {

    private static final int BASE64_MASK = 63;

    private static final long HASHKEY_MASK = 68719476735L;

    private static final char[] TABLE = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
        'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
        'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
        '5', '6', '7', '8', '9', '+', '_' };

    public static final int LENGTH = 6;

    private final String hash;

    /**
     * C'tor
     */
    public HashKey() {
        final long rnd = System.currentTimeMillis() * super.hashCode() & HASHKEY_MASK;

        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < LENGTH; i++) {

            sb.append(TABLE[(int) rnd & BASE64_MASK]);
        }
        this.hash = sb.toString();
    }

    /**
     * Convert from base 64 to decimal.
     *
     * @param key
     *            base64 key/hash
     *
     */
    public HashKey(final String hk) {

        this.hash = hk;
    }

    /**
     * Get hashcode as integer
     *
     */
    @Override
    public int hashCode() {
        return hash.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {

        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj.getClass() != getClass()) {
            return false;
        }

        // internal hash is long, while hashCode() is int

        final HashKey hk = (HashKey) obj;
        return this.hash.equals(hk.hash);
    }

    @Override
    public String toString() {
        return hash;
    }
}
