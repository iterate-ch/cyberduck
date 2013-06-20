package ch.cyberduck.core.exception;

import ch.cyberduck.core.AbstractTestCase;

import org.jets3t.service.CloudFrontServiceException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class CloudFrontServiceExceptionMappingServiceTest extends AbstractTestCase {

    @Test
    public void testLoginFailure() throws Exception {
        final CloudFrontServiceException f = new CloudFrontServiceException(
                "message", 403, "type", "InvalidClientTokenId", "The security token included in the request is invalid.", "detail", "id45"
        );
        assertTrue(new CloudFrontServiceExceptionMappingService().map(f) instanceof LoginFailureException);
        assertEquals("Login failed", new CloudFrontServiceExceptionMappingService().map(f).getMessage());
        assertEquals("The security token included in the request is invalid. detail.", new CloudFrontServiceExceptionMappingService().map(f).getDetail());
    }

    @Test
    public void testCloudfrontException() {
        CloudFrontServiceException e = new CloudFrontServiceException("message",
                1, "type", "errorCode", "errorMessage", "errorDetail", "Id");
        assertEquals("errorMessage. errorDetail.", new CloudFrontServiceExceptionMappingService().map(e).getDetail());
    }

    @Test
    public void testNullValues() {
        CloudFrontServiceException e = new CloudFrontServiceException("message",
                1, "type", "", "errorMessage", "", "Id");
        assertEquals("errorMessage.", new CloudFrontServiceExceptionMappingService().map(e).getDetail());
    }
}
