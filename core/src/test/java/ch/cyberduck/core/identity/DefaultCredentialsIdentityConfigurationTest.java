package ch.cyberduck.core.identity;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultCredentialsIdentityConfigurationTest {

    @Test
    public void testGetUserCredentials() throws Exception {
        final Host bookmark = new Host(new TestProtocol(), "h", new Credentials("u", null));
        final DefaultCredentialsIdentityConfiguration configuration = new DefaultCredentialsIdentityConfiguration(
                bookmark,
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
        bookmark.getCredentials().setUsername("a");
        assertEquals(null, configuration.getCredentials("u"));
    }
}
