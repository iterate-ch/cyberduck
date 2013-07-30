package ch.cyberduck.core.identity;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.junit.Test;

import java.util.UUID;

import com.amazonaws.services.identitymanagement.model.MalformedPolicyDocumentException;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class AWSIdentityConfigurationTest extends AbstractTestCase {

    @Test
    public void testCreateUser() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(), new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final AWSIdentityConfiguration iam = new AWSIdentityConfiguration(host, new DisabledLoginController());
        final String username = UUID.randomUUID().toString();
        try {
            iam.create(username, "{}");
            fail();
        }
        catch(BackgroundException e) {
            assertEquals("Cannot write user configuration", e.getMessage());
            assertEquals(MalformedPolicyDocumentException.class, e.getCause().getClass());
            iam.delete(username);
        }
        iam.create(username, "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Action\": \"s3:*\",\n" +
                "      \"Resource\": \"*\"\n" +
                "    }\n" +
                "  ]\n" +
                "}");
        assertNotNull(iam.getCredentials(username));
        iam.delete(username);
        assertNull(iam.getCredentials(username));
    }

    @Test(expected = LoginCanceledException.class)
    public void testCreateUserAuthenticationFailure() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(), new Credentials(
                "key", "secret"
        ));
        new AWSIdentityConfiguration(host, new DisabledLoginController()).create("u", "{}");
    }
}
