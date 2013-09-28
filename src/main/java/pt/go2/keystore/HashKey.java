package pt.go2.keystore;

class HashKey {

	private final long MAX_HASH = 68719476735l;

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
	public HashKey(final byte[] key) {
		this.key = key;

		long hash = 0;

		for (byte b : key) {

			long inc;

			if (b >= 'a' && b <= 'z') {
				inc = b - 'a';

			} else if (b >= 'A' && b <= 'Z') {
				inc = b - 'A' + 26;

			} else if (b >= '0' && b <= '9') {
				inc = b - '0' + 52;

			} else if (b == '_') {
				inc = 62;

			} else {
				inc = 63;
			}

			hash *= 64;
			hash += inc;
		}

		this.hash = hash;
	}

	/**
	 * Use this
	 * 
	 */
	public void rehash() {
		generateHash();
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

		if (obj == null)
			return false;

		if (obj == this)
			return true;

		if (obj.getClass() != getClass())
			return false;

		// internal hash is long, while hashCode() is int

		HashKey hash = (HashKey) obj;
		return this.hash == hash.hash;
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

		this.key = new byte[6];

		int i = 5;
		long v = this.hash;

		while (i >= 0) {

			long b = v & (64 - 1);
			v >>= 6;

			if (b < 26) {
				b += 'a';
			} else if (b < 52) {
				b += -26 + 'A';
			} else if (b < 62) {
				b += -52 + '0';
			} else if (b == 62) {
				b = '_';
			} else {
				b = '-';
			}
			this.key[i] = (byte) b;
			i--;
		}
	}
}
