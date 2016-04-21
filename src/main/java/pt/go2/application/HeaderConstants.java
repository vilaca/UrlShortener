package pt.go2.application;

enum HeaderConstants {

    // Request headers
    REQUEST_HEADER_ACCEPT_ENCODING("Accept-encoding"),
    REQUEST_HEADER_HOST("Host"),
    REQUEST_HEADER_REFERER("Referer"),
    REQUEST_HEADER_USER_AGENT("User-Agent"),

    // Response headers
    RESPONSE_HEADER_CACHE_CONTROL("Cache-Control"),
    RESPONSE_HEADER_CONTENT_ENCODING("Content-Encoding"),
    RESPONSE_HEADER_CONTENT_TYPE("Content-Type"),
    RESPONSE_HEADER_EXPIRES("Expires"),
    RESPONSE_HEADER_LOCATION("Location"),
    RESPONSE_HEADER_SERVER("Server");
    
    private final String value;
    
    HeaderConstants(String value)
    {
        this.value = value;
    }
    
    @Override
    public String toString()
    {
        return value;
    }
}
