package ch.cyberduck.core;

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.threading.CancelCallback;

public class NullSession extends Session<Void> {

    public NullSession(Host h) {
        super(h);
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public Void open(HostKeyCallback c) throws BackgroundException {
        return null;
    }

    @Override
    protected Void connect(final HostKeyCallback key) throws BackgroundException {
        return null;
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel, final Cache<Path> cache) throws BackgroundException {
        throw new LoginCanceledException();
    }

    @Override
    protected void logout() {
        //
    }

    public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
        return new AttributedList<Path>() {
            @Override
            public Path get(final Path reference) {
                return reference;
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(Class<T> type) {
        if(type == Write.class) {
            return (T) new NullWriteFeature(this);
        }
        if(type == Read.class) {
            return (T) new NullReadFeature();
        }
        return super._getFeature(type);
    }

}

