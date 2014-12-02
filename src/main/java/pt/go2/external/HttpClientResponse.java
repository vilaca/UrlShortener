package pt.go2.external;

import java.util.Collections;
import java.util.List;

import org.eclipse.jetty.http.HttpStatus;

public class HttpClientResponse {

    private final int status;
    private final List<String> records;

    public HttpClientResponse(int status) {
        this.status = status;
        this.records = Collections.emptyList();
    }

    public HttpClientResponse(List<String> lines) {
        this.status = HttpStatus.OK_200;
        this.records = Collections.unmodifiableList(lines);
    }

    public int status() {
        return status;
    }

    public List<String> records() {
        return records;
    }
}
