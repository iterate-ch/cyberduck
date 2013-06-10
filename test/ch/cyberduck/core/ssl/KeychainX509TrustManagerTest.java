package ch.cyberduck.core.ssl;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Keychain;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static com.ibm.icu.impl.Assert.fail;

/**
 * @version $Id$
 */
public class KeychainX509TrustManagerTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        Keychain.register();
    }

    @Test
    public void testCheckServerTrusted() throws Exception {
        final KeychainX509TrustManager m = new KeychainX509TrustManager(new TrustManagerHostnameCallback() {
            @Override
            public String getHostname() {
                return "cyberduck.ch";
            }
        });
        InputStream inStream = new FileInputStream("test/ch/cyberduck/core/ssl/OXxlRDVcWqdPEvFm.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        try {
            m.checkServerTrusted(new X509Certificate[]{cert}, "RSA");
        }
        catch(CertificateException e) {
            fail(e);
        }
    }
}