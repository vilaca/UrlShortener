package pt.go2.storage;

/**
 * Each Hashkey unequivocally identifies an Url
 */
public class HashKey {

	private static final int SIXBITS = 6;

	private static final int BASE_64 = 64;

	private static final int ALPHABET_SIZE = 26;

	private static final int UPPERANDLOWERCASE = ALPHABET_SIZE * 2;

	private static final int UPPERANDLOWERCASEANDNUMERALS = UPPERANDLOWERCASE + 10;

	public static final int LENGTH = 6;

	private static final long MAX_HASH = 68719476735L;

	// hash key as Base10
	private long hash;

	// hash key as Base64
	private byte[] key;

	/**
	 * C'tor
	 */
	public HashKey() {
		generateHash();
	}

	/**
	 * Convert from base 64 to decimal.
	 * 
	 * @param key
	 *            base64 key/hash
	 */
	public HashKey(final String key) {

		this.key = key.getBytes();

		for (byte b : this.key) {

			long inc;

			if (b >= 'a' && b <= 'z') {
				inc = b - 'a';

			} else if (b >= 'A' && b <= 'Z') {
				inc = b - 'A' + ALPHABET_SIZE;

			} else if (b >= '0' && b <= '9') {
				inc = b - '0' + UPPERANDLOWERCASE;

			} else if (b == '_') {

				inc = UPPERANDLOWERCASEANDNUMERALS;

			} else {
				inc = BASE_64 - 1;
			}

			hash *= BASE_64;
			hash += inc;
		}
	}

	/**
	 * Get key in base64 format as a byte array
	 * 
	 * @return key
	 */
	public byte[] getBytes() {
		return this.key;
	}

	/**
	 * Get hashcode as integer
	 * 
	 */
	@Override
	public int hashCode() {
		return (int) hash;
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

		HashKey hk = (HashKey) obj;
		return this.hash == hk.hash;
	}

	@Override
	public String toString() {
		return new String(key);
	}

	/**
	 * Generate a "random" hashkey
	 */
	private void generateHash() {
		final long millis = System.currentTimeMillis();
		this.hash = super.hashCode();
		this.hash *= millis;
		// 36 bit limit
		this.hash = this.hash & (MAX_HASH);
		encode64();
	}

	/**
	 * Encode hashkey as a base64 byte array
	 */
	private void encode64() {

		this.key = new byte[LENGTH];

		int i = LENGTH - 1;
		long v = this.hash;

		while (i >= 0) {

			long b = v & (BASE_64 - 1);
			v >>= SIXBITS;

			if (b < ALPHABET_SIZE) {
				b += 'a';
			} else if (b < UPPERANDLOWERCASE) {
				b += -ALPHABET_SIZE + 'A';
			} else if (b < UPPERANDLOWERCASEANDNUMERALS) {
				b += -UPPERANDLOWERCASE + '0';
			} else if (b == UPPERANDLOWERCASEANDNUMERALS) {
				b = '_';
			} else {
				b = '-';
			}
			this.key[i] = (byte) b;
			i--;
		}
	}
}
