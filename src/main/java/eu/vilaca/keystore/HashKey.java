package eu.vilaca.keystore;

public class HashKey {

	private byte[] key;
	private long hash;

	public HashKey() {
		final long millis = System.currentTimeMillis();
		this.hash = super.hashCode();
		this.hash *= millis;
		// 36 bit limit
		this.hash = this.hash & (68719476736l-1);
		Encode64();
	}

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

	public void rehash() {
		this.hash += System.currentTimeMillis() % 256;
		Encode64();
	}

	public byte[] getBytes() {
		return this.key;
	}

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

		// internal hash is long, white hashCode() is int

		HashKey hash =(HashKey) obj;
		return this.hash == hash.hash;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (byte b : key) {
			sb.append((char) b);
		}
		return sb.toString();
	}

	private void Encode64() {

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
