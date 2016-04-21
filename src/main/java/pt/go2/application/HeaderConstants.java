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
    RESPONSE_HEADER_SERVER("Server"),

    // mime types
    MIME_APP_JAVASCRIPT("application/javascript"),
    MIME_APP_JSON("application/json"),

    MIME_IMG_JPEG("image/jpeg"),
    MIME_IMG_GIF("image/gif"),
    MIME_IMG_PNG("image/png"),

    MIME_TEXT_CSS("text/css"),
    MIME_TEXT_HTML("text/html"),
    MIME_TEXT_PLAIN("text/plain"),
    MIME_TEXT_XML("text/xml");
    
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
