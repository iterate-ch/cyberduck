package ch.cyberduck.core.exception;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;

import org.jets3t.service.ServiceException;
import org.junit.Test;

import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class ServiceExceptionMappingServiceTest extends AbstractTestCase {

    @Test
    public void testLoginFailure() throws Exception {
        final ServiceException f = new ServiceException();
        f.setResponseCode(403);
        assertTrue(new ServiceExceptionMappingService().map(f) instanceof LoginFailureException);
    }

    @Test
    public void testMapping() {
        assertEquals("message.", new ServiceExceptionMappingService().map(new ServiceException("message")).getMessage());
        assertEquals("Exceeded 403 retry limit (1).", new ServiceExceptionMappingService().map(
                new ServiceException("Exceeded 403 retry limit (1).")).getMessage());
    }

    @Test
    public void testDNSFailure() {
        assertEquals("custom",
                new ServiceExceptionMappingService().map("custom", new ServiceException("message", new UnknownHostException("h")), new Host("h")).getMessage());
        assertEquals("h.",
                new ServiceExceptionMappingService().map("custom", new ServiceException("message", new UnknownHostException("h")), new Host("h")).getDetailedCauseMessage());
    }

    @Test
    public void testCustomMessage() {
        assertEquals("custom",
                new ServiceExceptionMappingService().map("custom", new ServiceException("message"), new Host("h")).getMessage());
        assertEquals("message.",
                new ServiceExceptionMappingService().map("custom", new ServiceException("message"), new Host("h")).getDetailedCauseMessage());
    }

    @Test
    public void testIAMFailure() {
        assertEquals("The IAM policy must allow the action s3:GetBucketLocation on the resource arn:aws:s3:::endpoint-9a527d70-d432-4601-b24b-735e721b82c9",
                new ServiceExceptionMappingService().map("The IAM policy must allow the action s3:GetBucketLocation on the resource arn:aws:s3:::endpoint-9a527d70-d432-4601-b24b-735e721b82c9", new ServiceException("message"), new Host("h")).getMessage());
        assertEquals("message.",
                new ServiceExceptionMappingService().map("The IAM policy must allow the action s3:GetBucketLocation on the resource arn:aws:s3:::endpoint-9a527d70-d432-4601-b24b-735e721b82c9", new ServiceException("message"), new Host("h")).getDetailedCauseMessage());
    }
}
