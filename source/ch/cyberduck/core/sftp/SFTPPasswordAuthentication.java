package ch.cyberduck.core.sftp;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @version $Id:$
 */
public class SFTPPasswordAuthentication {
    private static final Logger log = Logger.getLogger(SFTPPasswordAuthentication.class);

    private SFTPSession session;

    public SFTPPasswordAuthentication(final SFTPSession session) {
        this.session = session;
    }

    public boolean authenticate(final Host host, final LoginController prompt)
            throws IOException, LoginCanceledException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Login using password authentication with credentials %s", host.getCredentials()));
        }
        if(session.getClient().isAuthMethodAvailable(host.getCredentials().getUsername(), "password")) {
            return session.getClient().authenticateWithPassword(host.getCredentials().getUsername(), host.getCredentials().getPassword());
        }
        return false;
    }
}
