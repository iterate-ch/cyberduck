package ch.cyberduck.core.identity;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.dav.DAVProtocol;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class DefaultCredentialsIdentityConfigurationTest {

    @Test
    public void testGetUserCredentials() throws Exception {
        final Credentials credentials = new Credentials("u", "p");
        assertEquals(credentials, new DefaultCredentialsIdentityConfiguration(new Host(new DAVProtocol(),
                "h", credentials)).getCredentials("a"));
    }
}
