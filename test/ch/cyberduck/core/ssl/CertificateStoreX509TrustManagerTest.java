package ch.cyberduck.core.ssl;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.CertificateStore;
import ch.cyberduck.core.Keychain;

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
public class CertificateStoreX509TrustManagerTest extends AbstractTestCase {

    @Test
    public void testCheckServerTrusted() throws Exception {
        final CertificateStoreX509TrustManager m = new CertificateStoreX509TrustManager(new TrustManagerHostnameCallback() {
            @Override
            public String getTarget() {
                return "cyberduck.ch";
            }
        }, new Keychain());
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

    @Test(expected = CertificateException.class)
    public void testCheckServerTrustedFailure() throws Exception {
        final CertificateStoreX509TrustManager m = new CertificateStoreX509TrustManager(new TrustManagerHostnameCallback() {
            @Override
            public String getTarget() {
                return "cyberduck.ch";
            }
        }, new CertificateStore() {
            @Override
            public boolean isTrusted(final String hostname, final X509Certificate[] certs) {
                return false;
            }

            @Override
            public boolean display(final X509Certificate[] certificates) {
                throw new UnsupportedOperationException();
            }

            @Override
            public X509Certificate choose(final String[] issuers, final String hostname, final String prompt) {
                throw new UnsupportedOperationException();
            }
        }
        );
        InputStream inStream = new FileInputStream("test/ch/cyberduck/core/ssl/OXxlRDVcWqdPEvFm.cer");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        m.checkServerTrusted(new X509Certificate[]{cert}, "RSA");
    }
}