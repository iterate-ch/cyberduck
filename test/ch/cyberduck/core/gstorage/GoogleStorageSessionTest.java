package ch.cyberduck.core.gstorage;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.AclPermission;
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

    @Test
    public void testConnect() throws Exception {
        final Host host = new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname(), new Credentials(
                properties.getProperty("google.projectid"), null
        ));
        final GoogleStorageSession session = new GoogleStorageSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback(), session));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore() {
            @Override
            public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
                if(user.equals("Google OAuth2 Access Token")) {
                    return properties.getProperty("google.accesstoken");
                }
                if(user.equals("Google OAuth2 Refresh Token")) {
                    return properties.getProperty("google.refreshtoken");
                }
                return null;
            }
        }, new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        assertTrue(session.isSecured());
        session.close();
    }

    @Test(expected = LoginFailureException.class)
    public void testConnectInvalidRefreshToken() throws Exception {
        final Host host = new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname(), new Credentials(
                properties.getProperty("google.projectid"), null
        ));
        final GoogleStorageSession session = new GoogleStorageSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback(), session));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore() {
            @Override
            public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
                if(user.equals("Google OAuth2 Access Token")) {
                    return properties.getProperty("google.accesstoken");
                }
                if(user.equals("Google OAuth2 Refresh Token")) {
                    return "a";
                }
                return null;
            }
        }, new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
    }

    @Test
    public void testConnectInvalidAccessTokenRefreshToken() throws Exception {
        final Host host = new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname(), new Credentials(
                properties.getProperty("google.projectid"), null
        ));
        final GoogleStorageSession session = new GoogleStorageSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback(), session));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore() {
            @Override
            public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
                if(user.equals("Google OAuth2 Access Token")) {
                    // Mark as not expired
                    Preferences.instance().setProperty("google.storage.oauth.expiry", System.currentTimeMillis() + 60 * 1000);
                    return "a";
                }
                if(user.equals("Google OAuth2 Refresh Token")) {
                    return properties.getProperty("google.refreshtoken");
                }
                return null;
            }
        }, new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
    }

    @Test(expected = LoginFailureException.class)
    public void testConnectInvalidProjectId() throws Exception {
        final Host host = new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname(), new Credentials(
                properties.getProperty("google.projectid") + "1", null
        ));
        final GoogleStorageSession session = new GoogleStorageSession(host);
        session.open(new DisabledHostKeyCallback(), session);
        try {
            session.login(new DisabledPasswordStore() {
                @Override
                public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
                    if(user.equals("Google OAuth2 Access Token")) {
                        return properties.getProperty("google.accesstoken");
                    }
                    if(user.equals("Google OAuth2 Refresh Token")) {
                        return properties.getProperty("google.refreshtoken");
                    }
                    return null;
                }
            }, new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        }
        catch(BackgroundException e) {
//            assertEquals("Access denied. 4082461033721 is not a valid project id spec. Please contact your web hosting service provider for assistance. Please contact your web hosting service provider for assistance.", e.getDetail());
//            assertEquals("Invalid argument. Please contact your web hosting service provider for assistance.", e.getDetail());
            assertEquals("Listing directory failed.", e.getMessage());
            throw e;
        }
    }

    @Test(expected = LoginCanceledException.class)
    public void testConnectMissingKey() throws Exception {
        final Host host = new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname(), new Credentials(
                properties.getProperty("google.projectid"), null
        ));
        final GoogleStorageSession session = new GoogleStorageSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback(), session));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController() {
            @Override
            public void prompt(final Protocol protocol, final Credentials credentials,
                               final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                assertEquals("OAuth2 Authentication", title);
                throw new LoginCanceledException();
            }
        }, null, new DisabledTranscriptListener());
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailure() throws Exception {
        final Host host = new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname(), new Credentials(
                "a", "s"
        ));
        final GoogleStorageSession session = new GoogleStorageSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback(), session));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        try {
            session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
            fail();
        }
        catch(LoginFailureException e) {
            throw e;
        }
    }

    @Test
    public void testFeatures() {
        assertNotNull(new GoogleStorageSession(new Host("t")).getFeature(AclPermission.class));
        assertNotNull(new GoogleStorageSession(new Host("t")).getFeature(DistributionConfiguration.class));
        assertNotNull(new GoogleStorageSession(new Host("t")).getFeature(IdentityConfiguration.class));
        assertNotNull(new GoogleStorageSession(new Host("t")).getFeature(Logging.class));
        assertNotNull(new GoogleStorageSession(new Host("t")).getFeature(Headers.class));
        assertNull(new GoogleStorageSession(new Host("t")).getFeature(Lifecycle.class));
        assertNull(new GoogleStorageSession(new Host("t")).getFeature(Versioning.class));
    }
}
