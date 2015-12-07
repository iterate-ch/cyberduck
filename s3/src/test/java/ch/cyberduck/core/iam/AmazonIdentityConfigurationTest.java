package ch.cyberduck.core.iam;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.threading.DefaultFailureDiagnostics;
import ch.cyberduck.core.threading.FailureDiagnostics;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class AmazonIdentityConfigurationTest extends AbstractTestCase {

    @Test
    public void testCreateUser() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final AmazonIdentityConfiguration iam = new AmazonIdentityConfiguration(host);
        final String username = UUID.randomUUID().toString();
//        try {
//            iam.create(username, "{}", new DisabledLoginController());
//            fail();
//        }
//        catch(BackgroundException e) {
//            assertEquals("Cannot write user configuration.", e.getMessage());
//            assertEquals(MalformedPolicyDocumentException.class, e.getCause().getClass());
//            iam.delete(username, new DisabledLoginController());
//        }
        iam.create(username, "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Action\": \"s3:*\",\n" +
                "      \"Resource\": \"*\"\n" +
                "    }\n" +
                "  ]\n" +
                "}", new DisabledLoginCallback());
        assertNotNull(iam.getCredentials(username));
        iam.delete(username, new DisabledLoginCallback());
        assertNull(iam.getCredentials(username));
    }

    @Test(expected = LoginCanceledException.class)
    public void testCreateUserAuthenticationFailure() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                "key", "secret"
        ));
        new AmazonIdentityConfiguration(host).create("u", "{}", new DisabledLoginCallback());
    }

    @Test(expected = ConnectionTimeoutException.class)
    public void testTimeout() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final AmazonIdentityConfiguration iam = new AmazonIdentityConfiguration(host, 1);
        final String username = UUID.randomUUID().toString();
        try {
            iam.create(username, "{}", new DisabledLoginCallback());
            fail();
        }
        catch(BackgroundException e) {
            assertEquals("Cannot write user configuration.", e.getMessage());
            assertTrue(new DefaultFailureDiagnostics().determine(e) == FailureDiagnostics.Type.network);
//            assertEquals("Unable to execute HTTP request: Connect to iam.amazonaws.com:443 timed out.", e.getDetail());
            throw e;
        }
    }
}
