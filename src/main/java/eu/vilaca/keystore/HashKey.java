package eu.vilaca.keystore;

import java.util.Arrays;

public class HashKey {

	private byte[] key;
	private long hash;

	public HashKey() {
		this.hash = super.hashCode();
		this.hash *= System.currentTimeMillis();
	}

	public HashKey(final byte[] key) {
		this.key = key;

		int hash = 0;
		for (byte b : key) {
			hash *= 10;
			hash += b;
		}
		this.hash = hash;
	}

	public byte[] getBytes() {
		if ( this.key != null)
			return this.key;
		Encode64();
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

		return this.hash == ((HashKey) obj).hash
				&& Arrays.equals(this.key, ((HashKey) obj).key);
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
