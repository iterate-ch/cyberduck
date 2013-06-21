package ch.cyberduck.core;

import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @version $Id$
 */
public class NullPath extends Path {

    public NullPath(final String path, final int type) {
        super(new NullSession(new Host("test")), path, type);
    }

    @Override
    public AttributedList<Path> list() {
        return AttributedList.emptyList();
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public Session getSession() {
        return new NullSession(new Host("test"));
    }

    @Override
    public InputStream read(final TransferStatus status) throws BackgroundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void download(final BandwidthThrottle throttle, final StreamListener listener, final TransferStatus status) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OutputStream write(final TransferStatus status) throws BackgroundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void upload(final BandwidthThrottle throttle, final StreamListener listener, final TransferStatus status) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void mkdir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(final LoginController prompt) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rename(final Path renamed) {
        //
    }
}
