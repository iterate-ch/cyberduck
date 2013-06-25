package ch.cyberduck.core.transfer.move;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class MoveTransferFilterTest {

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
        MoveTransferFilter f = new MoveTransferFilter(files);
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
        MoveTransferFilter f = new MoveTransferFilter(files);
        assertFalse(f.accept(new NullSession(new Host("h")), source));
    }

    @Test
    public void testPrepareFile() throws Exception {
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        final NullPath source = new NullPath("a", Path.FILE_TYPE);
        source.attributes().setSize(1L);
        files.put(source, new NullPath("a", Path.FILE_TYPE));
        MoveTransferFilter f = new MoveTransferFilter(files);
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
        MoveTransferFilter f = new MoveTransferFilter(files);
        final TransferStatus status = f.prepare(new NullSession(new Host("h")), source);
        assertEquals(0L, status.getLength());
    }
}
