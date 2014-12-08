package ch.cyberduck.core.ssl;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.DefaultCertificateStore;
import ch.cyberduck.core.DisabledCertificateStore;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

import static com.ibm.icu.impl.Assert.fail;

/**
 * @version $Id$
 */
public class CertificateStoreX509TrustManagerTest extends AbstractTestCase {

    @Test(expected = CertificateException.class)
    public void testCheckExpired() throws Exception {
        final CertificateStoreX509TrustManager m = new CertificateStoreX509TrustManager(new TrustManagerHostnameCallback() {
            @Override
            public String getTarget() {
                return "cyberduck.ch";
            }
        }, new DefaultCertificateStore());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(
                new FileInputStream("test/ch/cyberduck/core/ssl/OXxlRDVcWqdPEvFm.cer")
        );
        try {
            m.checkServerTrusted(new X509Certificate[]{cert}, "RSA");
        }
        catch(CertificateException e) {
            fail(e);
        }
    }

    @Test(expected = CertificateException.class)
    public void testCheckServerTrustedFailure() throws Exception {
        final CertificateStoreX509TrustManager m = new CertificateStoreX509TrustManager(new TrustManagerHostnameCallback() {
            @Override
            public String getTarget() {
                return "cyberduck.ch";
            }
        }, new DisabledCertificateStore() {
            @Override
            public boolean isTrusted(final String hostname, List<X509Certificate> certificates) {
                return false;
            }
        }
        );
        InputStream inStream = new FileInputStream("test/ch/cyberduck/core/ssl/OXxlRDVcWqdPEvFm.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        m.checkServerTrusted(new X509Certificate[]{cert}, "RSA");
    }
}