package ch.cyberduck.core;

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.shared.AppendWriteFeature;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.InputStream;
import java.io.OutputStream;

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
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, CancelCallback cancel) throws BackgroundException {
        //
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel, final Cache<Path> cache) throws BackgroundException {
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
        if(type == Write.class) {
            return (T) new AppendWriteFeature(this) {
                @Override
                public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
                    throw new BackgroundException();
                }

                @Override
                public boolean temporary() {
                    return false;
                }

                @Override
                public boolean random() {
                    return false;
                }
            };
        }
        if(type == Read.class) {
            return (T) new Read() {
                @Override
                public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean offset(final Path file) throws BackgroundException {
                    return false;
                }
            };
        }
        return super.getFeature(type);
    }
}

