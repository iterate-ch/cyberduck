package ch.cyberduck.core;

import ch.cyberduck.binding.WindowController;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KeychainCertificateStoreTest {

    @Test
    public void testTrustedEmptyCertificates() throws Exception {
        final KeychainCertificateStore k = new KeychainCertificateStore(new WindowController() {
            @Override
            protected String getBundleName() {
                return null;
            }
        });
        assertFalse(k.isTrusted("cyberduck.ch", Collections.emptyList()));
    }

    @Test
    public void testTrusted() throws Exception {
        final KeychainCertificateStore k = new KeychainCertificateStore(new WindowController() {
            @Override
            protected String getBundleName() {
                return null;
            }
        });
        InputStream inStream = new FileInputStream("src/test/resources/OXxlRDVcWqdPEvFm.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        assertTrue(k.isTrusted("test.cyberduck.ch", Collections.singletonList(cert)));
    }

    @Test
    public void testTrustedHostnameMismatch() throws Exception {
        final KeychainCertificateStore k = new KeychainCertificateStore(new WindowController() {
            @Override
            protected String getBundleName() {
                return null;
            }
        });
        InputStream inStream = new FileInputStream("src/test/resources/OXxlRDVcWqdPEvFm.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        assertTrue(k.isTrusted("s.test.cyberduck.ch", Collections.singletonList(cert)));
    }
}
