package ch.cyberduck.core.exception;

import org.jets3t.service.CloudFrontServiceException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @version $Id:$
 */
public class CloudFrontServiceExceptionMappingServiceTest {

    @Test
    public void testLoginFailure() throws Exception {
        final CloudFrontServiceException f = new CloudFrontServiceException(
                "message", 403, "type", "InvalidClientTokenId", "The security token included in the request is invalid.", "detail", "id45"
        );
        Assert.assertTrue(new CloudFrontServiceExceptionMappingService().map(f) instanceof LoginFailureException);
        Assert.assertEquals("The security token included in the request is invalid. detail.", new CloudFrontServiceExceptionMappingService().map(f).getMessage());
    }

    @Test
    public void testCloudfrontException() {
        CloudFrontServiceException e = new CloudFrontServiceException("message",
                1, "type", "errorCode", "errorMessage", "errorDetail", "Id");
        Assert.assertEquals("errorMessage. errorDetail.", new CloudFrontServiceExceptionMappingService().map(e).getMessage());
    }

    @Test
    public void testNullValues() {
        CloudFrontServiceException e = new CloudFrontServiceException("message",
                1, "type", "", "errorMessage", "", "Id");
        Assert.assertEquals("errorMessage.", new CloudFrontServiceExceptionMappingService().map(e).getMessage());
    }
}
