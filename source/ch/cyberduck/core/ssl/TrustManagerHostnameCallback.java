package ch.cyberduck.core.ssl;

/**
 * @version $Id:$
 */
public interface TrustManagerHostnameCallback {
    /**
     * @return Hostname to use when validating server certificate.
     */
    String getHostname();
}
