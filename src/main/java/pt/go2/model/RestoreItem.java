package pt.go2.model;

/**
 * Class to hold data records from restore files
 */
public class RestoreItem {

	private final String key;
	private final String value;

	public RestoreItem(final String k, final String v) {
		this.key = k;
		this.value = v;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}
}
