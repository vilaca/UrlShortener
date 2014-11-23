package pt.go2.fileio;

/**
 * Class to hold data records from restore files
 */
public class RestoreItem {

    private final String key;
    private final String value;

    public RestoreItem(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}