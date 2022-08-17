package ch.cyberduck.core.s3;

import ch.cyberduck.core.Host;

import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.JetS3tRequestAuthorizer;
import org.junit.Test;

import javax.net.ssl.SSLException;
import java.net.SocketException;
import java.net.URI;

import static org.junit.Assert.assertTrue;

public class S3HttpRequestRetryHandlerTest {

    @Test
    public void testRetryRequest() {
        final S3HttpRequestRetryHandler h = new S3HttpRequestRetryHandler(new Host(new S3Protocol()), new JetS3tRequestAuthorizer() {
            @Override
            public void authorizeHttpRequest(final String bucketName, final HttpUriRequest httpMethod, final HttpContext context, final String forceRequestSignatureVersion) throws ServiceException {
                //
            }
        }, 1);
        final HttpClientContext context = new HttpClientContext();
        context.setAttribute(HttpCoreContext.HTTP_REQUEST, new HttpHead(URI.create("https://cyberduck.io/")));
        assertTrue(h.retryRequest(new SSLException(new SocketException("Broken pipe")), 1, context));
    }
}
