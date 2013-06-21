package ch.cyberduck.core;

import ch.cyberduck.core.threading.BackgroundException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class ConnectionCheckService {
    private static final Logger log = Logger.getLogger(ConnectionCheckService.class);

    private LoginController prompt;

    public ConnectionCheckService(final LoginController prompt) {
        this.prompt = prompt;
    }

    /**
     * Assert that the connection to the remote host is still alive.
     * Open connection if needed.
     *
     * @param session Session
     * @throws BackgroundException If opening connection fails
     */
    public void check(final Session session) throws BackgroundException {
        if(!session.isConnected()) {
            if(StringUtils.isBlank(session.getHost().getHostname())) {
                if(StringUtils.isBlank(session.getHost().getProtocol().getDefaultHostname())) {
                    throw new ConnectionCanceledException();
                }
                // If hostname is missing update with default
                session.getHost().setHostname(session.getHost().getProtocol().getDefaultHostname());
            }
            this.connect(session);
        }
        else {
            // The session is still supposed to be connected
            try {
                // Send a 'no operation command' to make sure the session is alive
                session.noop();
            }
            catch(BackgroundException e) {
                log.warn(String.format("No operation command failed for session %s. Attempt to reopen connection", session));
                // Try to reconnect once more
                this.connect(session);
            }
        }
    }

    private void connect(final Session session) throws BackgroundException {
        if(session.isConnected()) {
            // Close the underlying socket first
            session.interrupt();
        }
        session.open();
        LoginService login = new LoginService(prompt);
        login.login(session);
    }
}