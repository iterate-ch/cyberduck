package ch.cyberduck.core;

import ch.cyberduck.core.io.BandwidthThrottle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @version $Id$
 */
public class NullPath extends Path {

    public NullPath(final String path, final int type) {
        super(path, type);
    }

    @Override
    protected AttributedList<Path> list(final AttributedList<Path> children) {
        return AttributedList.emptyList();
    }

    @Override
    public Session getSession() {
        return new NullSession(new Host("test"));
    }

    @Override
    public InputStream read(final boolean check) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void download(final BandwidthThrottle throttle, final StreamListener listener, final boolean check, final boolean quarantine) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OutputStream write(final boolean check) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void upload(final BandwidthThrottle throttle, final StreamListener listener, final boolean check) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void mkdir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rename(final AbstractPath renamed) {
        throw new UnsupportedOperationException();
    }
}
