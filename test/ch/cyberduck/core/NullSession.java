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
    public FTPClient open(HostKeyCallback c) throws BackgroundException {
        return null;
    }

    @Override
    public void login(final PasswordStore keychain, final LoginCallback prompt) throws BackgroundException {
        //
    }

    @Override
    protected void logout() {
        //
    }

    public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
        return AttributedList.emptyList();
    }

    @Override
    public <T> T getFeature(Class<T> type) {
        if(type.equals(ch.cyberduck.core.features.Attributes.class)) {
            return (T) new ch.cyberduck.core.features.Attributes() {
                @Override
                public PathAttributes find(Path file) throws BackgroundException {
                    return file.attributes();
                }

                @Override
                public ch.cyberduck.core.features.Attributes withCache(Cache cache) {
                    return this;
                }
            };
        }
        return super.getFeature(type);
    }
}

