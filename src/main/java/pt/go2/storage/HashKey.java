package pt.go2.storage;

import java.util.Arrays;

/**
 * Each Hashkey unequivocally identifies an Url
 */
public class HashKey {

    private static final byte[] BASE64_CHARS = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
            'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', '+', '_' };

    // 64^6 - max possible hashkeys for (6 chrs, base64)

    private static final long HASHKEY_MASK = 68719476735L;

    private static final int BASE64_MASK = 63;

    private static final int BASE64_BIT_LEN = 6;

    public static final int LENGTH = 6;

    private final byte[] hash;

    /**
     * C'tor
     *
     */
    public HashKey(byte[] hash) {
        this.hash = hash;
    }

    /**
     * Static method - Use when you want to generate the Hashkey
     * 
     * @return
     */
    public static HashKey create() {

        // TODO current time is not a good enough seed, must use random

        long rnd = System.currentTimeMillis() * HASHKEY_MASK;

        final HashKey hk = new HashKey(new byte[LENGTH]);

        for (int i = 0; i < hk.hash.length; i++) {

            hk.hash[i] = BASE64_CHARS[(int) rnd & BASE64_MASK];

            rnd >>= BASE64_BIT_LEN;
        }

        return hk;
    }

    /**
     * Get internal representation
     * 
     * @return
     */
    public byte[] getHash() {
        return hash;
    }

    /**
     * Get hashcode as integer
     *
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(hash);
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

        for (int i = 0; i < LENGTH; i++) {
            if (this.hash[i] != ((HashKey) obj).hash[i]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return new String(hash);
    }
}
