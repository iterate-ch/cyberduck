package ch.cyberduck.core.identity;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Protocol;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class DefaultCredentialsIdentityConfigurationTest {

    @Test
    public void testGetUserCredentials() throws Exception {
        final Credentials credentials = new Credentials("u", "p");
        assertEquals(credentials, new DefaultCredentialsIdentityConfiguration(new Host(Protocol.WEBDAV,
                "h", credentials)).getUserCredentials("a"));
    }
}
