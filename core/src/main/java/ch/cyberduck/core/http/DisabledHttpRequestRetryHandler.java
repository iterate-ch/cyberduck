package ch.cyberduck.core.http;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

public class DisabledHttpRequestRetryHandler implements HttpRequestRetryHandler {
    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        return false;
    }
}
