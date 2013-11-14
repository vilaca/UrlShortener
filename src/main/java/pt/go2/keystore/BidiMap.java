package pt.go2.keystore;

import java.util.HashMap;
import java.util.Map;

/**
 * Bidirectional Map class
 *
 * @param <H>
 * @param <U>
 */
class BidiMap<H,U> {

	// hash to URL
	protected final Map<H, U> hash2Url;

	// URL to hash
	protected final Map<U, H> url2Hash;

	public BidiMap() {
		this.hash2Url = new HashMap<H,U>();
		this.url2Hash = new HashMap<U,H>();
	}

	void put(H hk, U uri)
	{
		this.hash2Url.put(hk, uri);
		this.url2Hash.put(uri, hk);		
	}

	H getUrl2Hash(U uri) {
		return url2Hash.get(uri);
	}

	U get(H hashKey) {
		return hash2Url.get(hashKey);
	}

	boolean contains(HashKey hk) {
		return hash2Url.containsKey(hk);
	}
	
	void remove(HashKey hk, Uri uri) {
		hash2Url.remove(hk);
		url2Hash.remove(uri);
	}
}
