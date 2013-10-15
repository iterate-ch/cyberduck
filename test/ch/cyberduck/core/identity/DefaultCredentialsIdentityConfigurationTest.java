package ch.cyberduck.core.identity;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.dav.DAVProtocol;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class DefaultCredentialsIdentityConfigurationTest extends AbstractTestCase {

    @Test
    public void testGetUserCredentials() throws Exception {
        final DefaultCredentialsIdentityConfiguration configuration = new DefaultCredentialsIdentityConfiguration(
                new Host(new DAVProtocol(), "h", new Credentials("u", null)),
                new DisabledPasswordStore() {
                    @Override
                    public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
                        if("u".equals(user)) {
                            return "p";
                        }
                        return null;
                    }
                });
        assertEquals(new Credentials("u", "p"), configuration.getCredentials("u"));
        assertEquals(null, configuration.getCredentials("b"));
    }
}
