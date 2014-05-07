package ch.cyberduck.core.sftp;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.CancelCallback;

/**
 * @version $Id$
 */
public interface SFTPAuthentication {

    /**
     * @return True if authentication is complete
     */
    boolean authenticate(Host host, LoginCallback controller, CancelCallback cancel)
            throws BackgroundException;
}
