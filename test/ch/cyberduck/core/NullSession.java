package ch.cyberduck.core;

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.ftp.FTPClient;
import ch.cyberduck.core.ftp.FTPSession;

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
    public FTPClient open(HostKeyController c) throws BackgroundException {
        this.fireConnectionWillOpenEvent();
        this.fireConnectionDidOpenEvent();
        return null;
    }

    @Override
    public void login(final PasswordStore keychain, final LoginController prompt) throws BackgroundException {
        //
    }

    @Override
    protected void logout() {
        //
    }

    @Override
    public <T> T getFeature(final Class<T> type, final LoginController prompt) {
        return null;
    }

    public AttributedList<Path> list(final Path file) {
        return AttributedList.emptyList();
    }
}
