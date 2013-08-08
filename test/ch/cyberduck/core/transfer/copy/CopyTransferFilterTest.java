package ch.cyberduck.core.transfer.copy;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class CopyTransferFilterTest extends AbstractTestCase {

    @Test
    public void testAcceptDirectoryNew() throws Exception {
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        final Path source = new Path("a", Path.DIRECTORY_TYPE);
        files.put(source, new Path("a", Path.DIRECTORY_TYPE));
        CopyTransferFilter f = new CopyTransferFilter(new NullSession(new Host("target")), files);
        assertTrue(f.accept(new NullSession(new Host("h")), source, new TransferStatus()));
    }

    @Test
    public void testAcceptDirectoryExists() throws Exception {
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        final Path source = new Path("a", Path.DIRECTORY_TYPE);
        files.put(source, new Path("a", Path.DIRECTORY_TYPE));
        CopyTransferFilter f = new CopyTransferFilter(new NullSession(new Host("target")), files);
        assertFalse(f.accept(new NullSession(new Host("h")) {
            @Override
            public boolean exists(final Path path) throws BackgroundException {
                return true;
            }
        }, source, new TransferStatus().exists(true)));
    }

    @Test
    public void testPrepareFile() throws Exception {
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        final Path source = new Path("a", Path.FILE_TYPE);
        source.attributes().setSize(1L);
        files.put(source, new Path("a", Path.FILE_TYPE));
        CopyTransferFilter f = new CopyTransferFilter(new NullSession(new Host("target")), files);
        final TransferStatus status = f.prepare(new NullSession(new Host("h")), source, new TransferStatus());
        assertEquals(1L, status.getLength());
    }

    @Test
    public void testPrepareDirectory() throws Exception {
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        final Path source = new Path("a", Path.DIRECTORY_TYPE);
        source.attributes().setSize(1L);
        final Path target = new Path("a", Path.DIRECTORY_TYPE) {

            NullSession session = new NullSession(new Host("t"));

        };
        files.put(source, target);
        CopyTransferFilter f = new CopyTransferFilter(new NullSession(new Host("target")), files);
        final TransferStatus status = f.prepare(new NullSession(new Host("h")), source, new TransferStatus());
        assertEquals(0L, status.getLength());
    }

    @Test
    public void testComplete() throws Exception {
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        final Path source = new Path("a", Path.FILE_TYPE);
        source.attributes().setSize(1L);
        source.attributes().setPermission(new Permission(777));
        final Long time = System.currentTimeMillis();
        source.attributes().setModificationDate(time);
        final boolean[] timestampWrite = new boolean[1];
        final boolean[] permissionWrite = new boolean[1];
        final Path target = new Path("a", Path.FILE_TYPE);
        files.put(source, target);
        CopyTransferFilter f = new CopyTransferFilter(new NullSession(new Host("target")) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                if(type.equals(Timestamp.class)) {
                    return (T) new Timestamp() {

                        @Override
                        public void setTimestamp(final Path file, final Long created, final Long modified, final Long accessed) throws BackgroundException {
                            assertEquals(time, modified);
                            timestampWrite[0] = true;
                        }
                    };
                }
                if(type.equals(UnixPermission.class)) {
                    return (T) new UnixPermission() {
                        @Override
                        public void setUnixOwner(final Path file, final String owner) throws BackgroundException {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setUnixGroup(final Path file, final String group) throws BackgroundException {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setUnixPermission(final Path file, final Permission permission) throws BackgroundException {
                            assertEquals(new Permission(777), permission);
                            permissionWrite[0] = true;
                        }
                    };
                }
                return null;
            }
        }, files);
        Preferences.instance().setProperty("queue.upload.preserveDate", true);
        final TransferStatus status = new TransferStatus();
        final NullSession session = new NullSession(new Host("h"));
        status.setLength(1L);
        f.complete(session, source, new TransferOptions(), status, session);
        assertFalse(permissionWrite[0]);
        assertFalse(timestampWrite[0]);
        status.setCurrent(1L);
        f.complete(session, source, new TransferOptions(), status, session);
        assertTrue(permissionWrite[0]);
        assertTrue(timestampWrite[0]);
    }
}
