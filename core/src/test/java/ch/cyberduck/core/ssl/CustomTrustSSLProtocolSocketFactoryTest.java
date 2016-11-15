package ch.cyberduck.core.ssl;

import ch.cyberduck.core.DisabledCertificateStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class CustomTrustSSLProtocolSocketFactoryTest {

    @Test
    public void testGetSSLContext() throws Exception {
        assertNotNull(new CustomTrustSSLProtocolSocketFactory(new DefaultX509TrustManager(), new CertificateStoreX509KeyManager(
                new DisabledCertificateStore(),
                new Host(new TestProtocol()))).getSSLContext());
    }
}
