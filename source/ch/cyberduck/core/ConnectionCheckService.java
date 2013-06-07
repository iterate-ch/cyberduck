package ch.cyberduck.core;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.SocketException;

/**
 * @version $Id:$
 */
public class ConnectionCheckService {
    private static final Logger log = Logger.getLogger(ConnectionCheckService.class);

    public void check(final Session session) throws IOException {
        try {
            if(!session.isConnected()) {
                if(StringUtils.isBlank(session.getHost().getHostname())) {
                    if(StringUtils.isBlank(session.getHost().getProtocol().getDefaultHostname())) {
                        log.warn(String.format("No default hostname configured for protocol %s", session.getHost().getProtocol()));
                        throw new ConnectionCanceledException();
                    }
                    // If hostname is missing update with default
                    session.getHost().setHostname(session.getHost().getProtocol().getDefaultHostname());
                }
                // If not connected anymore, reconnect the session
                session.connect();
            }
            else {
                // The session is still supposed to be connected
                try {
                    // Send a 'no operation command' to make sure the session is alive
                    session.noop();
                }
                catch(IOException e) {
                    // Close the underlying socket first
                    session.interrupt();
                    // Try to reconnect once more
                    session.connect();
                }
            }
        }
        catch(SocketException e) {
            if(e.getMessage().equals("Software caused connection abort")) {
                // Do not report as failed if socket opening interrupted
                log.warn("Supressed socket exception:" + e.getMessage());
                throw new ConnectionCanceledException(e.getMessage(), e);
            }
            if(e.getMessage().equals("Socket closed")) {
                // Do not report as failed if socket opening interrupted
                log.warn("Supressed socket exception:" + e.getMessage());
                throw new ConnectionCanceledException(e.getMessage(), e);
            }
            throw e;
        }
        catch(SSLHandshakeException e) {
            log.error(String.format("SSL Handshake failed for host %s", session.getHost()), e);
            if(e.getCause() instanceof sun.security.validator.ValidatorException) {
                throw e;
            }
            // Most probably caused by user dismissing ceritifcate. No trusted certificate found.
            throw new ConnectionCanceledException(e.getMessage(), e);
        }
    }
}
