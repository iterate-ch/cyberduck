package ch.cyberduck.core.azure;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class AzureSessionTest {

    @Test
    public void testFeatures() throws Exception {
        final Host host = new Host(new AzureProtocol(), "test.cyberduck.ch");
        final Session session = new AzureSession(host);
        assertNotNull(session.getFeature(AclPermission.class));
        assertNotNull(session.getFeature(Directory.class));
        assertNotNull(session.getFeature(Delete.class));
        assertNotNull(session.getFeature(Touch.class));
    }

    @Test
    public void testConnect() throws Exception {
        final Host host = new Host(new AzureProtocol(), "kahy9boj3eib.blob.core.windows.net", new Credentials(
            System.getProperties().getProperty("azure.account"), System.getProperties().getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, PathCache.empty(), new DisabledCancelCallback());
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
    }

    @Test
    public void testConnectSharedAccessSignature() throws Exception {
        final Host host = new Host(new AzureProtocol() {
            @Override
            public boolean isUsernameConfigurable() {
                return false;
            }

            @Override
            public boolean isPasswordConfigurable() {
                return false;
            }

            @Override
            public boolean isTokenConfigurable() {
                return true;
            }
        }, "kahy9boj3eib.blob.core.windows.net", new Credentials(
            null, null
        ).withToken("?sv=2017-07-29&ss=bfqt&srt=sco&sp=rwdlacup&se=2030-05-20T04:29:30Z&st=2018-05-09T20:29:30Z&spr=https&sig=bMKAZ3tXmX%2B56%2Bb5JhHAeWnMOpMp%2BoYlHDIAZVAjHzE%3D"));
        final AzureSession session = new AzureSession(host);
        final LoginConnectionService connect = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener());
        connect.connect(session, PathCache.empty(), new DisabledCancelCallback());
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
    }

    @Test(expected = LoginCanceledException.class)
    public void testConnectInvalidKey() throws Exception {
        final Host host = new Host(new AzureProtocol(), "kahy9boj3eib.blob.core.windows.net", new Credentials(
            System.getProperties().getProperty("azure.account"), "6h9BmTcabGajIE/AVGzgu9JcC15JjrzkjdAIe+2daRK8XlyVdYT6zHtFMwXOtrlCw74jX9R0w4GN56vKQjOpVA=="
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, String username, String title, String reason, LoginOptions options) throws LoginCanceledException {
                assertEquals("Server failed to authenticate the request. Make sure the value of Authorization header is formed correctly including the signature. Please contact your web hosting service provider for assistance.",
                    reason);
                return super.prompt(bookmark, username, title, reason, options);
            }
        }, new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, PathCache.empty(), new DisabledCancelCallback());
    }

    @Test(expected = LoginCanceledException.class)
    public void testConnectKeyNotBase64() throws Exception {
        final Host host = new Host(new AzureProtocol(), "kahy9boj3eib.blob.core.windows.net", new Credentials(
            System.getProperties().getProperty("azure.account"), "6h9B"
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, String username, String title, String reason, LoginOptions options) throws LoginCanceledException {
                assertEquals("Server failed to authenticate the request. Make sure the value of Authorization header is formed correctly including the signature. Please contact your web hosting service provider for assistance.",
                    reason);
                return super.prompt(bookmark, username, title, reason, options);
            }
        }, new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, PathCache.empty(), new DisabledCancelCallback());
    }
}
