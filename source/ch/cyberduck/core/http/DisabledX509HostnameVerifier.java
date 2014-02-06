package ch.cyberduck.core.http;

import ch.cyberduck.core.ssl.TrustManagerHostnameCallback;

import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.cert.X509Certificate;

/**
 * @version $Id$
 */
public class DisabledX509HostnameVerifier implements X509HostnameVerifier, TrustManagerHostnameCallback {
    private static final Logger log = Logger.getLogger(DisabledX509HostnameVerifier.class);

    /**
     * Target hostname of current request stored as thread local
     */
    private ThreadLocal<String> target
            = new ThreadLocal<String>();

    @Override
    public void verify(final String host, final SSLSocket socket) throws IOException {
        log.debug(String.format("Hostname verification disabled for %s handled in system trust manager", host));
        target.set(host);
    }

    @Override
    public void verify(final String host, final X509Certificate cert) throws SSLException {
        log.debug(String.format("Hostname verification disabled for %s handled in system trust manager", host));
        target.set(host);
    }

    @Override
    public void verify(final String host, final String[] cns, final String[] subjectAlts) throws SSLException {
        log.debug(String.format("Hostname verification disabled for %s handled in system trust manager", host));
        target.set(host);
    }

    @Override
    public boolean verify(final String host, final SSLSession sslSession) {
        log.debug(String.format("Hostname verification disabled for %s handled in system trust manager", host));
        target.set(host);
        return true;
    }

    @Override
    public String getTarget() {
        return target.get();
    }

    public void setTarget(final String target) {
        this.target.set(target);
    }
}
