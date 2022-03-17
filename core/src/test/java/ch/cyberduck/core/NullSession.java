package ch.cyberduck.core;

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.threading.CancelCallback;

public class NullSession extends Session<Void> implements ListService {

    public NullSession(Host h) {
        super(h);
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public Void connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) {
        return null;
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        throw new LoginCanceledException();
    }

    @Override
    protected void logout() {
        //
    }

    @Override
    public AttributedList<Path> list(final Path folder, final ListProgressListener listener) throws BackgroundException {
        listener.chunk(folder, AttributedList.emptyList());
        return AttributedList.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(Class<T> type) {
        if(type == ListService.class) {
            return (T) this;
        }
        if(type == Write.class) {
            return (T) new NullWriteFeature(this);
        }
        if(type == Read.class) {
            return (T) new NullReadFeature();
        }
        if(type == Move.class) {
            return (T) new NullMoveFeature();
        }
        if(type == Directory.class) {
            return (T) new NullDirectoryFeature();
        }
        return super._getFeature(type);
    }

}

