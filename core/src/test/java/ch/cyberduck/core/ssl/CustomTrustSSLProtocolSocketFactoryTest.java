package ch.cyberduck.core.ssl;

import ch.cyberduck.core.DisabledCertificateIdentityCallback;
import ch.cyberduck.core.DisabledCertificateStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.junit.Test;

import javax.net.ssl.SSLSocket;
import java.util.Arrays;

import static org.junit.Assert.*;

public class CustomTrustSSLProtocolSocketFactoryTest {

    @Test
    public void testGetSSLContext() {
        assertNotNull(new CustomTrustSSLProtocolSocketFactory(new DefaultX509TrustManager(), new CertificateStoreX509KeyManager(
            new DisabledCertificateIdentityCallback(),
            new Host(new TestProtocol()), new DisabledCertificateStore()
        )).getSSLContext());
    }

    @Test
    public void testConfigureCipherBlacklist() throws Exception {
        final CustomTrustSSLProtocolSocketFactory factory = new CustomTrustSSLProtocolSocketFactory(new DefaultX509TrustManager(), new CertificateStoreX509KeyManager(
                new DisabledCertificateIdentityCallback(),
                new Host(new TestProtocol()), new DisabledCertificateStore()
        ));
        final SSLSocket socket = (SSLSocket) factory.getSSLContext().getSocketFactory().createSocket();
        final String blacklisted = socket.getEnabledCipherSuites()[0];
        final Preferences preferences = PreferencesFactory.get();
        preferences.setProperty("connection.ssl.cipher.blacklist", blacklisted);
        try {
            factory.configure(socket, socket.getEnabledProtocols());
            assertTrue(socket.getEnabledCipherSuites().length > 0);
            assertFalse(Arrays.asList(socket.getEnabledCipherSuites()).contains(blacklisted));
        }
        finally {
            preferences.deleteProperty("connection.ssl.cipher.blacklist");
        }
    }
}
