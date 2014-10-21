package ch.cyberduck.core.azure;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class AzureSessionTest extends AbstractTestCase {

    @Test
    public void testConnect() throws Exception {
        final Host host = new Host(new AzureProtocol(), "cyberduck.blob.core.windows.net", new Credentials(
                properties.getProperty("azure.account"), properties.getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, Cache.<Path>empty());
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
    }

    @Test(expected = LoginCanceledException.class)
    public void testConnectInvalidKey() throws Exception {
        final Host host = new Host(new AzureProtocol(), "cyberduck.blob.core.windows.net", new Credentials(
                properties.getProperty("azure.account"), "6h9BmTcabGajIE/AVGzgu9JcC15JjrzkjdAIe+2daRK8XlyVdYT6zHtFMwXOtrlCw74jX9R0w4GN56vKQjOpVA=="
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials, String title, String reason, LoginOptions options) throws LoginCanceledException {
                assertEquals("Server failed to authenticate the request. Make sure the value of Authorization header is formed correctly including the signature. Please contact your web hosting service provider for assistance.",
                        reason);
                super.prompt(protocol, credentials, title, reason, options);
            }
        }, new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, Cache.<Path>empty());
    }

    @Test(expected = LoginCanceledException.class)
    public void testConnectKeyNotBase64() throws Exception {
        final Host host = new Host(new AzureProtocol(), "cyberduck.blob.core.windows.net", new Credentials(
                properties.getProperty("azure.account"), "6h9B"
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials, String title, String reason, LoginOptions options) throws LoginCanceledException {
                assertEquals("Server failed to authenticate the request. Make sure the value of Authorization header is formed correctly including the signature. Please contact your web hosting service provider for assistance.",
                        reason);
                super.prompt(protocol, credentials, title, reason, options);
            }
        }, new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, Cache.<Path>empty());
    }
}
