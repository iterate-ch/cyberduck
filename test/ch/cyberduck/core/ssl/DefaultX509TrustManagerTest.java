package ch.cyberduck.core.ssl;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * @version $Id$
 */
public class DefaultX509TrustManagerTest extends AbstractTestCase {

    @Test(expected = CertificateExpiredException.class)
    public void testCheckServerTrusted() throws Exception {
        final DefaultX509TrustManager m = new DefaultX509TrustManager();
        InputStream inStream = new FileInputStream("test/ch/cyberduck/core/ssl/OXxlRDVcWqdPEvFm.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        m.checkServerTrusted(new X509Certificate[]{cert}, "RSA");
    }
}