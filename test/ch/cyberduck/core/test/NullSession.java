package ch.cyberduck.core.test;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.ftp.FTPClient;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.threading.CancelCallback;

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
    public FTPClient open(HostKeyCallback c, final TranscriptListener transcript) throws BackgroundException {
        return null;
    }

    @Override
    public void login(final PasswordStore keychain, final LoginCallback prompt, CancelCallback cancel) throws BackgroundException {
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
                public ch.cyberduck.core.features.Attributes withCache(PathCache cache) {
                    return this;
                }
            };
        }
        return super.getFeature(type);
    }
}

