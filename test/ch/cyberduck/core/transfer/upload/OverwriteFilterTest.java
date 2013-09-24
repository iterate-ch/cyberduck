package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.NullSymlinkResolver;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class OverwriteFilterTest extends AbstractTestCase {

    @Test(expected = NotfoundException.class)
    public void testAcceptNotFoundFile() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver(), new NullSession(new Host("h")));
        // Local file does not exist
        assertFalse(f.accept(new Path("a", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public boolean exists() {
                        return false;
                    }
                };
            }
        }, new TransferStatus()));
    }

    @Test(expected = NotfoundException.class)
    public void testAcceptNotFoundDirectory() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver(), new NullSession(new Host("h")));
        // Local file does not exist
        assertFalse(f.accept(new Path("a", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t") {
                    @Override
                    public boolean exists() {
                        return false;
                    }
                };
            }
        }, new TransferStatus()));
    }

    @Test
    public void testAcceptRemoteExists() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver(), new NullSession(new Host("h")));
        assertTrue(f.accept(new Path("a", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t");
            }
        }, new TransferStatus()));
        assertTrue(f.accept(new Path("a", Path.DIRECTORY_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "t");
            }
        }, new TransferStatus()
        ));
    }

    @Test
    public void testSize() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver(), new NullSession(new Host("h")));
        assertEquals(1L, f.prepare(new Path("/t", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "/t") {
                    @Override
                    public Attributes attributes() {
                        return new PathAttributes(Path.FILE_TYPE) {
                            @Override
                            public long getSize() {
                                return 1L;
                            }
                        };
                    }
                };
            }
        }, new TransferStatus()).getLength(), 0L);
    }

    @Test
    public void testPermissionsNoChange() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver(), new NullSession(new Host("h")));
        final Path file = new Path("/t", Path.FILE_TYPE);
        file.setLocal(new NullLocal(null, "a"));
        assertFalse(f.prepare(file, new TransferStatus()).isComplete());
        assertEquals(Acl.EMPTY, file.attributes().getAcl());
        assertEquals(Permission.EMPTY, file.attributes().getPermission());
    }

    @Test
    public void testPermissionsExistsNoChange() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver(), new NullSession(new Host("h")));
        final Path file = new Path("/t", Path.FILE_TYPE);
        file.setLocal(new NullLocal(null, "a"));
        assertFalse(f.prepare(file, new TransferStatus()).isComplete());
        assertEquals(Acl.EMPTY, file.attributes().getAcl());
        assertEquals(Permission.EMPTY, file.attributes().getPermission());
    }

    @Test
    public void testTemporary() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new NullSymlinkResolver(), new NullSession(new Host("h")),
                new UploadFilterOptions().withTemporary(true));
        final Path file = new Path("/t", Path.FILE_TYPE);
        file.setLocal(new NullLocal(null, "a"));
        final TransferStatus status = f.prepare(file, new TransferStatus());
        assertNotNull(status.getRenamed());
        assertTrue(status.isRename());
        assertNotEquals(file, status.getRenamed());
        assertNotNull(status.getRenamed().getLocal());
        assertEquals(new NullLocal(null, "a"), status.getRenamed().getLocal());
    }
}