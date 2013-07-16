package ch.cyberduck.core.gstorage;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.AccessControlList;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Lifecycle;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.identity.IdentityConfiguration;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class GoogleStorageSessionTest extends AbstractTestCase {

    @Test(expected = LoginFailureException.class)
    public void testConnectRackspace() throws Exception {
        final Host host = new Host(Protocol.GOOGLESTORAGE_SSL, Protocol.GOOGLESTORAGE_SSL.getDefaultHostname(), new Credentials(
                properties.getProperty("googlestorage.key"), properties.getProperty("googlestorage.secret")
        ));
        final GoogleStorageSession session = new GoogleStorageSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController() {
            @Override
            public void prompt(final Protocol protocol, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                assertEquals("OAuth2 Authentication", title);
                throw new LoginCanceledException();
            }
        });
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailure() throws Exception {
        final Host host = new Host(Protocol.GOOGLESTORAGE_SSL, Protocol.GOOGLESTORAGE_SSL.getDefaultHostname(), new Credentials(
                "a", "s"
        ));
        final GoogleStorageSession session = new GoogleStorageSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        try {
            session.login(new DisabledPasswordStore(), new DisabledLoginController());
            fail();
        }
        catch(LoginFailureException e) {
            throw e;
        }
    }

    @Test
    public void testFeatures() {
        assertNotNull(new GoogleStorageSession(new Host("t")).getFeature(AccessControlList.class, null));
        assertNotNull(new GoogleStorageSession(new Host("t")).getFeature(DistributionConfiguration.class, null));
        assertNotNull(new GoogleStorageSession(new Host("t")).getFeature(IdentityConfiguration.class, null));
        assertNotNull(new GoogleStorageSession(new Host("t")).getFeature(Logging.class, null));
        assertNotNull(new GoogleStorageSession(new Host("t")).getFeature(Headers.class, null));
        assertNull(new GoogleStorageSession(new Host("t")).getFeature(Lifecycle.class, null));
        assertNull(new GoogleStorageSession(new Host("t")).getFeature(Versioning.class, null));
    }
}
