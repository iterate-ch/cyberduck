package ch.cyberduck.core.ssl;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NullKeychain;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * @version $Id:$
 */
public class DefaultX509TrustManagerTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        NullKeychain.register();
    }

    @Test(expected = CertificateException.class)
    public void testCheckServerTrusted() throws Exception {
        final KeychainX509TrustManager m = new KeychainX509TrustManager() {
            @Override
            public String getHostname() {
                return "cyberduck.ch";
            }
        };
        InputStream inStream = new FileInputStream("test/ch/cyberduck/core/ssl/*.cyberduck.ch (OXxlRDVcWqdPEvFm).cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        m.checkServerTrusted(new X509Certificate[]{cert}, "RSA");
    }
}
