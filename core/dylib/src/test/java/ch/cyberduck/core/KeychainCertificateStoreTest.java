package ch.cyberduck.core;

import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

public class KeychainCertificateStoreTest {

    @Test
    public void testTrustedEmptyCertificates() throws Exception {
        final KeychainCertificateStore k = new KeychainCertificateStore();
        assertFalse(k.verify(new DisabledCertificateTrustCallback(), "cyberduck.ch", Collections.emptyList()));
    }

    @Test
    public void testTrusted() throws Exception {
        final KeychainCertificateStore k = new KeychainCertificateStore();
        InputStream inStream = new FileInputStream("src/test/resources/OXxlRDVcWqdPEvFm.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        assertFalse(k.verify(new DisabledCertificateTrustCallback(), "test.cyberduck.ch", Collections.singletonList(cert)));
    }

    @Test
    public void testHostnameMismatchIpaddress() throws Exception {
        final KeychainCertificateStore k = new KeychainCertificateStore();
        InputStream inStream = new FileInputStream("src/test/resources/OXxlRDVcWqdPEvFm.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        assertFalse(k.verify(new DisabledCertificateTrustCallback(), "54.228.253.92", Collections.singletonList(cert)));
    }

    @Test
    public void testTrustedHostnameMismatch() throws Exception {
        final KeychainCertificateStore k = new KeychainCertificateStore();
        InputStream inStream = new FileInputStream("src/test/resources/OXxlRDVcWqdPEvFm.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        assertFalse(k.verify(new DisabledCertificateTrustCallback(), "s.test.cyberduck.ch", Collections.singletonList(cert)));
    }

    @Test
    public void testExpired() throws Exception {
        final KeychainCertificateStore k = new KeychainCertificateStore();
        InputStream inStream = new FileInputStream("src/test/resources/expired.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        assertFalse(k.verify(new DisabledCertificateTrustCallback(), "expired.badssl.com", Collections.singletonList(cert)));
    }

    @Test
    public void testRevoked() throws Exception {
        final KeychainCertificateStore k = new KeychainCertificateStore();
        InputStream inStream = new FileInputStream("src/test/resources/revoked.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        assertFalse(k.verify(new DisabledCertificateTrustCallback(), "revoked.badssl.com", Collections.singletonList(cert)));
    }

    @Test
    public void testSelfsigned() throws Exception {
        final KeychainCertificateStore k = new KeychainCertificateStore();
        InputStream inStream = new FileInputStream("src/test/resources/selfsigned.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        assertFalse(k.verify(new DisabledCertificateTrustCallback(), "self-signed.badssl.com", Collections.singletonList(cert)));
    }

    @Test
    public void testUntrustedRoot() throws Exception {
        final KeychainCertificateStore k = new KeychainCertificateStore();
        InputStream inStream = new FileInputStream("src/test/resources/untrusted-root.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        assertFalse(k.verify(new DisabledCertificateTrustCallback(), "untrusted-root.badssl.com", Collections.singletonList(cert)));
    }

    @Test
    public void testWrongHost() throws Exception {
        final KeychainCertificateStore k = new KeychainCertificateStore();
        InputStream inStream = new FileInputStream("src/test/resources/wrong.host.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        assertFalse(k.verify(new DisabledCertificateTrustCallback(), "wrong.host.badssl.com", Collections.singletonList(cert)));
    }

    @Test
    public void test15135() throws Exception {
        final KeychainCertificateStore k = new KeychainCertificateStore();
        InputStream inStream = new FileInputStream("src/test/resources/15135.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        assertThrows(ConnectionCanceledException.class, () -> k.verify(new DisabledCertificateTrustCallback(), "localhost", Collections.singletonList(cert)));
    }
}
