package ch.cyberduck.core;

import ch.cyberduck.core.exception.ConnectionCanceledException;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * @version $Id$
 */
public interface CertificateStore {

    /**
     * @param hostname     Hostname
     * @param certificates Certificate chain
     * @return True if trusted in Keychain
     */
    public abstract boolean isTrusted(String hostname, List<X509Certificate> certificates);

    /**
     * @param certificates X.509 certificates
     * @return False if display is not possible
     */
    public abstract boolean display(List<X509Certificate> certificates);

    /**
     * Prompt user for client certificate
     *
     * @param issuers  Distinguished names
     * @param hostname Client hostname
     * @param prompt   Display in certificate choose prompt
     * @return Null if no certificate selected
     */
    public X509Certificate choose(String[] issuers, String hostname, String prompt) throws ConnectionCanceledException;
}
