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
public class AzureSessionTest extends AbstractAzureTest {

    @Test
    public void testFeatures() {
        assertNotNull(session.getFeature(AclPermission.class));
        assertNotNull(session.getFeature(Directory.class));
        assertNotNull(session.getFeature(Delete.class));
        assertNotNull(session.getFeature(Touch.class));
    }

    @Test
    public void testConnect() throws Exception {
        assertTrue(session.isConnected());
    }

    @Test(expected = LoginCanceledException.class)
    public void testConnectInvalidKey() throws Exception {
        final Host host = new Host(new AzureProtocol(), "kahy9boj3eib.blob.core.windows.net", new Credentials(
                PROPERTIES.get("azure.user"), "6h9BmTcabGajIE/AVGzgu9JcC15JjrzkjdAIe+2daRK8XlyVdYT6zHtFMwXOtrlCw74jX9R0w4GN56vKQjOpVA=="
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, String username, String title, String reason, LoginOptions options) throws LoginCanceledException {
                assertEquals("Login kahy9boj3eib.blob.core.windows.net", title);
                assertEquals("Server failed to authenticate the request. Make sure the value of Authorization header is formed correctly including the signature. Please contact your web hosting service provider for assistance.", reason);
                return super.prompt(bookmark, username, title, reason, options);
            }
        }, new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, new DisabledCancelCallback());
    }

    @Test(expected = LoginCanceledException.class)
    public void testConnectKeyNotBase64() throws Exception {
        final Host host = new Host(new AzureProtocol(), "kahy9boj3eib.blob.core.windows.net", new Credentials(
                PROPERTIES.get("azure.user"), "6h9B"
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, String username, String title, String reason, LoginOptions options) throws LoginCanceledException {
                assertEquals("Login kahy9boj3eib.blob.core.windows.net", title);
                assertEquals("Server failed to authenticate the request. Make sure the value of Authorization header is formed correctly including the signature. Please contact your web hosting service provider for assistance.", reason);
                return super.prompt(bookmark, username, title, reason, options);
            }
        }, new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, new DisabledCancelCallback());
    }
}
