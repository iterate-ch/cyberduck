package ch.cyberduck.core.transfer.move;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class MoveTransferFilterTest extends AbstractTestCase {

    @Test
    public void testAcceptDirectoryNew() throws Exception {
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        final Path source = new Path("a", Path.DIRECTORY_TYPE);
        files.put(source, new Path("a", Path.DIRECTORY_TYPE));
        MoveTransferFilter f = new MoveTransferFilter(files);
        assertTrue(f.accept(new NullSession(new Host("h")), source));
    }

    @Test
    public void testAcceptDirectoryExists() throws Exception {
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        final Path source = new Path("a", Path.DIRECTORY_TYPE);
        files.put(source, new Path("a", Path.DIRECTORY_TYPE));
        MoveTransferFilter f = new MoveTransferFilter(files);
        assertFalse(f.accept(new NullSession(new Host("h")) {
            @Override
            public boolean exists(final Path path) throws BackgroundException {
                return true;
            }
        }, source));
    }

    @Test
    public void testPrepareFile() throws Exception {
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        final Path source = new Path("a", Path.FILE_TYPE);
        source.attributes().setSize(1L);
        files.put(source, new Path("a", Path.FILE_TYPE));
        MoveTransferFilter f = new MoveTransferFilter(files);
        final TransferStatus status = f.prepare(new NullSession(new Host("h")), source);
        assertEquals(1L, status.getLength());
    }

    @Test
    public void testPrepareDirectory() throws Exception {
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        final Path source = new Path("a", Path.DIRECTORY_TYPE);
        source.attributes().setSize(1L);
        final Path target = new Path("a", Path.DIRECTORY_TYPE);
        files.put(source, target);
        MoveTransferFilter f = new MoveTransferFilter(files);
        final TransferStatus status = f.prepare(new NullSession(new Host("h")), source);
        assertEquals(0L, status.getLength());
    }
}
