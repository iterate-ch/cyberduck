package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.DisabledUploadSymlinkResolver;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

public class OverwriteFilterTest {

    @Test(expected = NotfoundException.class)
    public void testAcceptNotFoundFile() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol())));
        // Local file does not exist
        assertFalse(f.accept(new Path("a", EnumSet.of(Path.Type.file)) {
                             }, new NullLocal("t") {
                                 @Override
                                 public boolean exists() {
                                     return false;
                                 }
                             }, new TransferStatus()
        ));
    }

    @Test(expected = NotfoundException.class)
    public void testAcceptNotFoundDirectory() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol())));
        // Local file does not exist
        assertFalse(f.accept(new Path("a", EnumSet.of(Path.Type.directory)) {
                             }, new NullLocal("t") {
                                 @Override
                                 public boolean exists() {
                                     return false;
                                 }
                             }, new TransferStatus()
        ));
    }

    @Test
    public void testAcceptRemoteExists() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol())));
        assertTrue(f.accept(new Path("a", EnumSet.of(Path.Type.directory)) {
        }, new NullLocal("t") {

            @Override
            public boolean exists() {
                return true;
            }

        }, new TransferStatus()));
        assertTrue(f.accept(new Path("a", EnumSet.of(Path.Type.directory)) {
                            }, new NullLocal("t") {

                                @Override
                                public boolean exists() {
                                    return true;
                                }

                            }, new TransferStatus()
        ));
    }

    @Test
    public void testSize() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol())));
        assertEquals(1L, f.prepare(new Path("/t", EnumSet.of(Path.Type.file)) {
                                   }, new NullLocal("/t") {
                                       @Override
                                       public LocalAttributes attributes() {
                                           return new LocalAttributes("/t") {
                                               @Override
                                               public long getSize() {
                                                   return 1L;
                                               }

                                           };
                                       }

                                       @Override
                                       public boolean isFile() {
                                           return true;
                                       }
                                   }, new TransferStatus()
        ).getLength(), 0L);
    }

    @Test
    public void testPermissionsNoChange() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol())));
        final Path file = new Path("/t", EnumSet.of(Path.Type.file));
        assertFalse(f.prepare(file, new NullLocal("t"), new TransferStatus()).isComplete());
        assertEquals(Acl.EMPTY, file.attributes().getAcl());
        assertEquals(Permission.EMPTY, file.attributes().getPermission());
    }

    @Test
    public void testPermissionsExistsNoChange() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol())));
        final Path file = new Path("/t", EnumSet.of(Path.Type.file));
        assertFalse(f.prepare(file, new NullLocal("/t"), new TransferStatus()).isComplete());
        assertEquals(Acl.EMPTY, file.attributes().getAcl());
        assertEquals(Permission.EMPTY, file.attributes().getPermission());
    }

    @Test
    public void testTemporary() throws Exception {
        final OverwriteFilter f = new OverwriteFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol())),
                new UploadFilterOptions().withTemporary(true));
        final Path file = new Path("/t", EnumSet.of(Path.Type.file));
        final TransferStatus status = f.prepare(file, new NullLocal("t"), new TransferStatus());
        assertNotNull(status.getRename());
        assertNotEquals(file, status.getRename().remote);
        assertNull(status.getRename().local);
        assertNotNull(status.getRename().remote);
    }

    @Test(expected = AccessDeniedException.class)
    public void testOverrideDirectoryWithFile() throws Exception {
        final AbstractUploadFilter f = new OverwriteFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol()))).withFinder(
                new Find() {
                    @Override
                    public boolean find(final Path file) throws BackgroundException {
                        if(file.getType().contains(Path.Type.file)) {
                            return false;
                        }
                        return true;
                    }

                    @Override
                    public Find withCache(final Cache<Path> cache) {
                        return this;
                    }
                }
        );
        f.prepare(new Path("a", EnumSet.of(Path.Type.file)), new NullLocal(System.getProperty("java.io.tmpdir"), "f"), new TransferStatus().exists(true));
    }

    @Test(expected = AccessDeniedException.class)
    public void testOverrideFileWithDirectory() throws Exception {
        final AbstractUploadFilter f = new OverwriteFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol()))).withFinder(
                new Find() {
                    @Override
                    public boolean find(final Path file) throws BackgroundException {
                        if(file.getType().contains(Path.Type.file)) {
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public Find withCache(final Cache<Path> cache) {
                        return this;
                    }
                }
        );
        f.prepare(new Path("a", EnumSet.of(Path.Type.directory)), new NullLocal(System.getProperty("java.io.tmpdir")), new TransferStatus().exists(true));
    }
}