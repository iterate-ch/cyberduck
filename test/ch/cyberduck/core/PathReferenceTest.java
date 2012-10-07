package ch.cyberduck.core;

import ch.cyberduck.core.io.BandwidthThrottle;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class PathReferenceTest {

    @Test
    public void testUnique() throws Exception {
        Path one = new Path("a", Path.FILE_TYPE) {
            @Override
            protected AttributedList<Path> list(final AttributedList<Path> children) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Session getSession() {
                throw new UnsupportedOperationException();
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
        };
        Path second = new Path("a", Path.FILE_TYPE) {
            @Override
            protected AttributedList<Path> list(final AttributedList<Path> children) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Session getSession() {
                throw new UnsupportedOperationException();
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
        };
        assertEquals(one.getReference(), second.getReference());
    }
}
