package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractTestCase;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.JetS3tRequestAuthorizer;
import org.junit.Test;

import javax.net.ssl.SSLException;
import java.net.SocketException;

import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class S3HttpREquestRetryHandlerTest extends AbstractTestCase {

    @Test
    public void testRetryRequest() throws Exception {
        final S3HttpREquestRetryHandler h = new S3HttpREquestRetryHandler(new JetS3tRequestAuthorizer() {
            @Override
            public void authorizeHttpRequest(final HttpUriRequest httpMethod, final HttpContext context) throws ServiceException {
                //
            }
        }, 1);
        assertTrue(h.retryRequest(new SSLException(new SocketException("Broken pipe")), 1, new HttpClientContext()));
    }
}
