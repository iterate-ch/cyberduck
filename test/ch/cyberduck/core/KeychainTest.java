package ch.cyberduck.core;

import ch.cyberduck.core.test.Depends;

import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class KeychainTest extends AbstractTestCase {

    @Test
    public void testFindPassword() throws Exception {
        final PasswordStore k = new Keychain();
        assertNull(k.getPassword("cyberduck.ch", "u"));
        assertNull(k.getPassword(Scheme.http, 80, "cyberduck.ch", "u"));
    }


    @Test
    public void testTrustedEmptyCertificates() throws Exception {
        final CertificateStore k = new Keychain();
        assertFalse(k.isTrusted("cyberduck.ch", Collections.<X509Certificate>emptyList()));
    }

    @Test
    public void testTrusted() throws Exception {
        final CertificateStore k = new Keychain();
        InputStream inStream = new FileInputStream("test/ch/cyberduck/core/ssl/OXxlRDVcWqdPEvFm.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        this.repeat(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                final boolean trusted = k.isTrusted("test.cyberduck.ch", Collections.<X509Certificate>singletonList(cert));
                assertTrue(trusted);
                return trusted;
            }
        }, 1);
    }

    @Test
    @Ignore
    public void testTrustedHostnameMismatch() throws Exception {
        final CertificateStore k = new Keychain();
        InputStream inStream = new FileInputStream("test/ch/cyberduck/core/ssl/OXxlRDVcWqdPEvFm.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        this.repeat(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                final boolean trusted = k.isTrusted("s.test.cyberduck.ch", Collections.<X509Certificate>singletonList(cert));
                assertTrue(trusted);
                return trusted;
            }
        }, 1);
    }
}
