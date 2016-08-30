package ch.cyberduck.core.ssl;

import ch.cyberduck.core.DisabledCertificateStore;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class CustomTrustSSLProtocolSocketFactoryTest {

    @Test
    public void testGetSSLContext() throws Exception {
        assertNotNull(new CustomTrustSSLProtocolSocketFactory(new DefaultX509TrustManager(), new CertificateStoreX509KeyManager(
                new DisabledCertificateStore()
        )).getSSLContext());
    }
}
