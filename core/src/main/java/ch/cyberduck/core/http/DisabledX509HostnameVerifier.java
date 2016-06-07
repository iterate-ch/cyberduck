package ch.cyberduck.core.http;

import org.apache.log4j.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class DisabledX509HostnameVerifier implements HostnameVerifier {
    private static final Logger log = Logger.getLogger(DisabledX509HostnameVerifier.class);

    @Override
    public boolean verify(final String host, final SSLSession sslSession) {
        log.debug(String.format("Hostname verification disabled for %s handled in system trust manager", host));
        return true;
    }
}
