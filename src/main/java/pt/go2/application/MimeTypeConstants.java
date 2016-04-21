package pt.go2.application;

enum MimeTypeConstants {

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
    
    MimeTypeConstants(String value)
    {
        this.value = value;
    }
    
    @Override
    public String toString()
    {
        return value;
    }
}
