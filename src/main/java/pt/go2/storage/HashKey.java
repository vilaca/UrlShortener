package pt.go2.storage;

/**
 * Each Hashkey unequivocally identifies an Url
 */
public class HashKey {

    private static final char[] BASE64_CHARS = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
            'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', '+', '_' };

    // 64^6 - max possible hashkeys for (6 chrs, base64) 
    
    private static final long HASHKEY_MASK = 68719476735L;
    
	private static final int BASE64_MASK = 63;

    private static final int BASE64_BIT_LEN = 6;

    // amt of chars in 
    
    public static final int LENGTH = 6;

    private final String hash;

    /**
     * C'tor - Use when already have the HashKey
     *
     * @param key
     *            base64 key/hash
     *
     */
    public HashKey(final String hk) {

        this.hash = hk;
    }

    /**
     * Static method - Use when you want to generate the Hashkey
     * 
     * @return
     */
    public static HashKey create() {

    	// TODO current time is not a good enough seed, must use random
    	
        long rnd = System.currentTimeMillis() * HASHKEY_MASK;

        final char[] hash = new char[LENGTH];

        for (int i = 0; i < LENGTH; i++) {

            hash[i] = BASE64_CHARS[(int) rnd & BASE64_MASK];

            rnd >>= BASE64_BIT_LEN;
        }

        return new HashKey(new String(hash));
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
