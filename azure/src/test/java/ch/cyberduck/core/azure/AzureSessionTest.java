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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class AzureSessionTest extends AbstractAzureTest {

    @Test
    public void testFeatures() {
        final Host host = new Host(new AzureProtocol(), "test.cyberduck.ch");
        final Session session = new AzureSession(host);
        assertNotNull(session.getFeature(AclPermission.class));
        assertNotNull(session.getFeature(Directory.class));
        assertNotNull(session.getFeature(Delete.class));
        assertNotNull(session.getFeature(Touch.class));
    }

    @Test
    public void testConnect() throws Exception {
        assertTrue(session.isConnected());
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
            null, null, "?sv=2017-07-29&ss=bfqt&srt=sco&sp=rwdlacup&se=2030-05-20T04:29:30Z&st=2018-05-09T20:29:30Z&spr=https&sig=bMKAZ3tXmX%2B56%2Bb5JhHAeWnMOpMp%2BoYlHDIAZVAjHzE%3D"));
        final AzureSession session = new AzureSession(host);
        final LoginConnectionService connect = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener());
        connect.connect(session, new DisabledCancelCallback());
        assertTrue(session.isConnected());
        connect.close(session);
        assertFalse(session.isConnected());
    }

    @Test
    public void testConnectSharedAccessSignaturePrompt() throws Exception {
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
            null, null, "?sv=2017-07-29&ss=bfqt&srt=sco&sp=rwdlacup&se=2030-05-20T04:29:30Z&st=2018-05-09T20:29:30Z&spr=https&sig=invalidbMKAZ3tXmX%2B56%2Bb5JhHAeWnMOpMp%2BoYlHDIAZVAjHzE%3D"));
        final AzureSession session = new AzureSession(host);
        final AtomicBoolean prompt = new AtomicBoolean();
        final LoginConnectionService connect = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                if(prompt.get()) {
                    throw new LoginCanceledException();
                }
                try {
                    return new Credentials(StringUtils.EMPTY, "?sv=2017-07-29&ss=bfqt&srt=sco&sp=rwdlacup&se=2030-05-20T04:29:30Z&st=2018-05-09T20:29:30Z&spr=https&sig=bMKAZ3tXmX%2B56%2Bb5JhHAeWnMOpMp%2BoYlHDIAZVAjHzE%3D");
                }
                finally {
                    prompt.set(true);
                }
            }
        }, new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener());
        connect.connect(session, new DisabledCancelCallback());
        assertTrue(session.isConnected());
        connect.close(session);
        assertFalse(session.isConnected());
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
                return super.prompt(bookmark, username, title, reason, options);
            }
        }, new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, new DisabledCancelCallback());
    }
}
