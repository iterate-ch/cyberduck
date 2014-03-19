package ch.cyberduck.core.sftp;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LoginCanceledException;

import java.io.IOException;

/**
 * @version $Id:$
 */
public interface SFTPAuthentication {

    boolean authenticate(Host host, LoginCallback controller)
            throws IOException, LoginCanceledException, AccessDeniedException;
}
