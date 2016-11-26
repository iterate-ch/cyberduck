package ch.cyberduck.core;

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.shared.AppendWriteFeature;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;

import java.io.InputStream;
import java.io.OutputStream;

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
    @SuppressWarnings("unchecked")
    protected <T> T _getFeature(Class<T> type) {
        if(type == Write.class) {
            return (T) new AppendWriteFeature(this) {
                @Override
                public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
                    return new NullOutputStream();
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
                    return new NullInputStream(0L);
                }

                @Override
                public boolean offset(final Path file) throws BackgroundException {
                    return false;
                }
            };
        }
        return super._getFeature(type);
    }
}

