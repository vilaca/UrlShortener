package pt.go2.keystore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import pt.go2.pagelets.RedirectPageLet;

public abstract class BidiMap {

	// hash to URL
	protected final Map<HashKey, RedirectPageLet> hash2Url;

	// URL to hash
	protected final Map<Uri, HashKey> url2Hash;

	/**
	 * User either Thread safe or Non-Thread safe factory method
	 */
	private BidiMap(Map<HashKey, RedirectPageLet> hash2Url,
			Map<Uri, HashKey> url2Hash) {

		this.hash2Url = hash2Url;
		this.url2Hash = url2Hash;
	}

	/**
	 * Thread safe factory method for class
	 */
	static BidiMap createSync() {
		
		return new BidiMap(new ConcurrentHashMap<HashKey, RedirectPageLet>(),
				new ConcurrentHashMap<Uri, HashKey>()) {

			@Override
			public boolean put(HashKey hk, Uri uri, RedirectPageLet redirect) {

				synchronized (this) {

					if (this.hash2Url.containsKey(hk)
							|| this.url2Hash.containsKey(uri)) {
						return false;
					}

					this.hash2Url.put(hk, redirect);
					this.url2Hash.put(uri, hk);
				}
				return true;
			}
		};
	}

	/**
	 * Non-Thread safe factory method for class
	 */
	static BidiMap create() {
		return new BidiMap(new HashMap<HashKey, RedirectPageLet>(),
				new HashMap<Uri, HashKey>()) {

			@Override
			public boolean put(HashKey hk, Uri uri, RedirectPageLet redirect) {

				this.hash2Url.put(hk, redirect);
				this.url2Hash.put(uri, hk);
				return true;

			}
		};
	}

	abstract boolean put(HashKey hk, Uri uri, RedirectPageLet redirect);

	HashKey getUrl2Hash(Uri uri) {
		return url2Hash.get(uri);
	}

	RedirectPageLet get(HashKey hashKey) {
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
