package pt.go2.external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpStatus;

public class HttpClientAdapter {

    private static final int TIMEOUT = 5;
    private static final Logger LOGGER = LogManager.getLogger();

    private HttpClientAdapter() {

    }

    static HttpClientResponse get(String address) {

        final HttpClient httpClient = new HttpClient();

        try {
            httpClient.start();
        } catch (final Exception e) {
            LOGGER.error("Can't start http client...", e);
            return null;
        }

        final InputStreamResponseListener listener = new InputStreamResponseListener();
        httpClient.newRequest(address).send(listener);

        Response response;
        try {
            response = listener.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            LOGGER.error(e);
            return null;
        }

        if (response.getStatus() != HttpStatus.OK_200) {
            return new HttpClientResponse(response.getStatus());
        }

        final List<String> records = new ArrayList<String>();

        try (InputStream responseContent = listener.getInputStream();
                final InputStreamReader isr = new InputStreamReader(responseContent, StandardCharsets.UTF_8);
                final BufferedReader br = new BufferedReader(isr);) {

            String record;

            while ((record = br.readLine()) != null) {
                records.add(record);
            }

        } catch (final IOException e) {
            LOGGER.error(e);
            return null;
        }

        return new HttpClientResponse(records);
    }
}
