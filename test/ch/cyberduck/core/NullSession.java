package ch.cyberduck.core;

import ch.cyberduck.core.ftp.FTPClient;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.threading.BackgroundException;

/**
 * @version $Id$
 */
public class NullSession extends FTPSession {

    public NullSession(Host h) {
        super(h);
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public FTPClient connect() throws BackgroundException {
        return null;
    }

    @Override
    public void login(final LoginController prompt) throws BackgroundException {
        //
    }

    @Override
    public void logout() {
        //
    }
}
