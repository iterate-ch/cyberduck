package ch.cyberduck.core;

import ch.cyberduck.binding.ProxyController;

import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;

import static org.junit.Assert.assertFalse;

public class KeychainCertificateStoreTest {

    @Test
    public void testTrustedEmptyCertificates() throws Exception {
        final KeychainCertificateStore k = new KeychainCertificateStore(new ProxyController());
        assertFalse(k.verify("cyberduck.ch", Collections.emptyList()));
    }

    @Test
    @Ignore
    public void testTrusted() throws Exception {
        final KeychainCertificateStore k = new KeychainCertificateStore(new ProxyController());
        InputStream inStream = new FileInputStream("src/test/resources/OXxlRDVcWqdPEvFm.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        assertFalse(k.verify("test.cyberduck.ch", Collections.singletonList(cert)));
    }

    @Test
    @Ignore
    public void testTrustedHostnameMismatch() throws Exception {
        final KeychainCertificateStore k = new KeychainCertificateStore(new ProxyController());
        InputStream inStream = new FileInputStream("src/test/resources/OXxlRDVcWqdPEvFm.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        assertFalse(k.verify("s.test.cyberduck.ch", Collections.singletonList(cert)));
    }
}
