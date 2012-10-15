package ch.cyberduck.core;

import java.security.cert.X509Certificate;

/**
 * @version $Id:$
 */
public class NullKeychain extends AbstractKeychain {

    public static void register() {
        KeychainFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends KeychainFactory {
        @Override
        protected AbstractKeychain create() {
            return new NullKeychain();
        }
    }

    @Override
    public boolean isTrusted(final String hostname, final X509Certificate[] certs) {
        return false;
    }

    @Override
    public boolean displayCertificates(final X509Certificate[] certificates) {
        return false;
    }

    @Override
    public X509Certificate chooseCertificate(final String[] issuers, final String hostname, final String prompt) {
        return null;
    }

    @Override
    public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
        return null;
    }

    @Override
    public void addPassword(final String serviceName, final String user, final String password) {
        //
    }

    @Override
    public String getPassword(final String hostname, final String user) {
        return null;
    }

    @Override
    public void addPassword(final Scheme scheme, final int port, final String hostname, final String user, final String password) {
        //
    }
}
