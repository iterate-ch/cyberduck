package ch.cyberduck.core.test;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.CancelCallback;

/**
 * @version $Id$
 */
public class NullSession extends Session<Void> {

    public NullSession(Host h) {
        super(h);
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public Void open(HostKeyCallback c, final TranscriptListener transcript) throws BackgroundException {
        return null;
    }

    @Override
    protected Void connect(final HostKeyCallback key) throws BackgroundException {
        return null;
    }

    @Override
    public void login(final PasswordStore keychain, final LoginCallback prompt, CancelCallback cancel) throws BackgroundException {
        //
    }

    @Override
    public void login(final PasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel, final Cache cache) throws BackgroundException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void logout() {
        //
    }

    public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
        return new AttributedList<Path>() {
            @Override
            public boolean contains(final Object o) {
                return true;
            }

            @Override
            public int indexOf(final Object o) {
                return 0;
            }

            @Override
            public Path get(final Path reference) {
                return reference;
            }
        };
    }

    @Override
    public <T> T getFeature(Class<T> type) {
        return super.getFeature(type);
    }
}

