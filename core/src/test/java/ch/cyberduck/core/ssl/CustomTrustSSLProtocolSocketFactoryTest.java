package ch.cyberduck.core.ssl;

import ch.cyberduck.core.DisabledCertificateIdentityCallback;
import ch.cyberduck.core.DisabledCertificateStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class CustomTrustSSLProtocolSocketFactoryTest {

    @Test
    public void testGetSSLContext() {
        assertNotNull(new CustomTrustSSLProtocolSocketFactory(new DefaultX509TrustManager(), new CertificateStoreX509KeyManager(
            new DisabledCertificateIdentityCallback(),
            new Host(new TestProtocol()), new DisabledCertificateStore()
        )).getSSLContext());
    }
}
