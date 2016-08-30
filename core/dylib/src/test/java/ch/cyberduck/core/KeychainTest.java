package ch.cyberduck.core;

import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;

import static org.junit.Assert.*;

@Ignore
public class KeychainTest {

    @Test
    public void testFindPassword() throws Exception {
        final Keychain k = new Keychain();
        assertNull(k.getPassword("cyberduck.ch", "u"));
        assertNull(k.getPassword(Scheme.http, 80, "cyberduck.ch", "u"));
    }

    @Test
    public void testTrustedEmptyCertificates() throws Exception {
        final Keychain k = new Keychain();
        assertFalse(k.isTrusted("cyberduck.ch", Collections.<X509Certificate>emptyList()));
    }

    @Test
    public void testTrusted() throws Exception {
        final Keychain k = new Keychain();
        InputStream inStream = new FileInputStream("src/test/resources/OXxlRDVcWqdPEvFm.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        final boolean trusted = k.isTrusted("test.cyberduck.ch", Collections.<X509Certificate>singletonList(cert));
        assertTrue(trusted);
    }

    @Test
    public void testTrustedHostnameMismatch() throws Exception {
        final Keychain k = new Keychain();
        InputStream inStream = new FileInputStream("src/test/resources/OXxlRDVcWqdPEvFm.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        final boolean trusted = k.isTrusted("s.test.cyberduck.ch", Collections.<X509Certificate>singletonList(cert));
        assertTrue(trusted);
    }
}
