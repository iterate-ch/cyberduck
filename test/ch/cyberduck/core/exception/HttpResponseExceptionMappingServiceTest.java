package ch.cyberduck.core.exception;

import org.apache.http.client.HttpResponseException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @version $Id:$
 */
public class HttpResponseExceptionMappingServiceTest {
    @Test
    public void testHttpException() {
        HttpResponseException e = new HttpResponseException(400, "message");
        Assert.assertEquals("400. message.", new HttpResponseExceptionMappingService().map(e).getMessage());
    }
}
