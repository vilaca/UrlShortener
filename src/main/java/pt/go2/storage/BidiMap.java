package pt.go2.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Bidirectional Map class
 *
 * @param <H>
 * @param <U>
 */
class BidiMap<H,U> {

	// hash to URL
	private final Map<H, U> hash2Url;

	// URL to hash
	private final Map<U, H> url2Hash;

	public BidiMap() {
		this.hash2Url = new HashMap<H,U>();
		this.url2Hash = new HashMap<U,H>();
	}

	synchronized void put(H hk, U uri)
	{
		this.hash2Url.put(hk, uri);
		this.url2Hash.put(uri, hk);		
	}

	synchronized H getUrl2Hash(U uri) {
		return url2Hash.get(uri);
	}

	synchronized U get(H hashKey) {
		return hash2Url.get(hashKey);
	}

	synchronized boolean contains(HashKey hk) {
		return hash2Url.containsKey(hk);
	}
	
	synchronized void remove(HashKey hk, Uri uri) {
		hash2Url.remove(hk);
		url2Hash.remove(uri);
	}

	public synchronized Set<U> getKeys() {
		return url2Hash.keySet();
	}
}
