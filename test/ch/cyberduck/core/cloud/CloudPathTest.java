package ch.cyberduck.core.cloud;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.StreamListener;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class CloudPathTest extends AbstractTestCase {

    @Test
    public void testIsContainer() throws Exception {
        assertFalse(new TestCloudPath("/", Path.VOLUME_TYPE).isContainer());
        assertTrue(new TestCloudPath("/t", Path.VOLUME_TYPE).isContainer());
        assertTrue(new TestCloudPath("/t/", Path.VOLUME_TYPE).isContainer());
        assertFalse(new TestCloudPath("/t/a", Path.VOLUME_TYPE).isContainer());
    }

    @Test
    public void testGetContainerName() throws Exception {
        assertEquals("t", new TestCloudPath("/t", Path.FILE_TYPE).getContainer().getName());
        assertEquals("t", new TestCloudPath("/t/a", Path.FILE_TYPE).getContainer().getName());
    }

    @Test
    public void testGetContainer() throws Exception {
        assertEquals("/t", new TestCloudPath("/t", Path.FILE_TYPE).getContainer().getAbsolute());

    }

    @Test
    public void testGetKey() throws Exception {
        assertEquals("d/f", new TestCloudPath("/c/d/f", Path.DIRECTORY_TYPE).getKey());
    }

    private static class TestCloudPath extends CloudPath {

        private TestCloudPath(String path, int type) {
            super(new NullSession(new Host("h")), path, type);
        }

        @Override
        public Path getParent() {
            return new TestCloudPath(Path.getParent(this.getAbsolute(), Path.DELIMITER), Path.DIRECTORY_TYPE);
        }

        @Override
        public AttributedList<Path> list() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Session getSession() {
            throw new UnsupportedOperationException();
        }

        @Override
        public InputStream read(TransferStatus status) throws BackgroundException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void download(BandwidthThrottle throttle, StreamListener listener, TransferStatus status) {
            throw new UnsupportedOperationException();
        }

        @Override
        public OutputStream write(TransferStatus status) throws BackgroundException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void upload(BandwidthThrottle throttle, StreamListener listener, TransferStatus status) {
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
        public void rename(Path renamed) {
            throw new UnsupportedOperationException();
        }
    }
}
