package ch.cyberduck.core.transfer.copy;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
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
        final NullPath source = new NullPath("a", Path.DIRECTORY_TYPE);
        files.put(source, new NullPath("a", Path.DIRECTORY_TYPE) {
            @Override
            public boolean exists() {
                return false;
            }
        });
        CopyTransferFilter f = new CopyTransferFilter(files);
        assertTrue(f.accept(new NullSession(new Host("h")), source));
    }

    @Test
    public void testAcceptDirectoryExists() throws Exception {
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        final NullPath source = new NullPath("a", Path.DIRECTORY_TYPE);
        files.put(source, new NullPath("a", Path.DIRECTORY_TYPE) {
            @Override
            public boolean exists() {
                return true;
            }
        });
        CopyTransferFilter f = new CopyTransferFilter(files);
        assertFalse(f.accept(new NullSession(new Host("h")), source));
    }

    @Test
    public void testPrepareFile() throws Exception {
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        final NullPath source = new NullPath("a", Path.FILE_TYPE);
        source.attributes().setSize(1L);
        files.put(source, new NullPath("a", Path.FILE_TYPE));
        CopyTransferFilter f = new CopyTransferFilter(files);
        final TransferStatus status = f.prepare(new NullSession(new Host("h")), source);
        assertEquals(1L, status.getLength());
    }

    @Test
    public void testPrepareDirectory() throws Exception {
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        final NullPath source = new NullPath("a", Path.DIRECTORY_TYPE);
        source.attributes().setSize(1L);
        final NullPath target = new NullPath("a", Path.DIRECTORY_TYPE) {

            @Override
            public boolean exists() {
                return false;
            }

            NullSession session = new NullSession(new Host("t"));

            @Override
            public Session getSession() {
                return session;
            }
        };
        files.put(source, target);
        CopyTransferFilter f = new CopyTransferFilter(files);
        final TransferStatus status = f.prepare(new NullSession(new Host("h")), source);
        assertEquals(0L, status.getLength());
    }

    @Test
    public void testComplete() throws Exception {
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        final NullPath source = new NullPath("a", Path.FILE_TYPE);
        source.attributes().setSize(1L);
        source.attributes().setPermission(new Permission(777));
        final long time = System.currentTimeMillis();
        source.attributes().setModificationDate(time);
        final boolean[] timestampWrite = new boolean[1];
        final boolean[] permissionWrite = new boolean[1];
        final NullPath target = new NullPath("a", Path.FILE_TYPE) {
            @Override
            public void writeTimestamp(final long created, final long modified, final long accessed) {
                assertEquals(time, modified);
                timestampWrite[0] = true;
            }

            @Override
            public void writeUnixPermission(final Permission permission) {
                assertEquals(new Permission(777), permission);
                permissionWrite[0] = true;
            }
        };
        files.put(source, target);
        CopyTransferFilter f = new CopyTransferFilter(files);
        Preferences.instance().setProperty("queue.upload.preserveDate", true);
        final TransferStatus status = new TransferStatus();
        f.complete(new NullSession(new Host("h")), source, new TransferOptions(), status);
        assertFalse(permissionWrite[0]);
        assertFalse(timestampWrite[0]);
        status.setComplete();
        f.complete(new NullSession(new Host("h")), source, new TransferOptions(), status);
        assertTrue(permissionWrite[0]);
        assertTrue(timestampWrite[0]);
    }
}
