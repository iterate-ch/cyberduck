package ch.cyberduck.core;

import java.security.cert.X509Certificate;

/**
 * @version $Id:$
 */
public interface CertificateStore {

    /**
     * @param hostname Hostname
     * @param certs    Certificate chain
     * @return True if trusted in Keychain
     */
    public abstract boolean isTrusted(String hostname, X509Certificate[] certs);

    /**
     * @param certificates X.509 certificates
     * @return False if display is not possible
     */
    public abstract boolean displayCertificates(X509Certificate[] certificates);

    /**
     * Prompt user for client certificate
     *
     * @param issuers  Distinguished names
     * @param hostname Client hostname
     * @param prompt   Display in certificate choose prompt
     * @return Null if no certificate selected
     */
    public X509Certificate chooseCertificate(String[] issuers, String hostname, String prompt);
}
