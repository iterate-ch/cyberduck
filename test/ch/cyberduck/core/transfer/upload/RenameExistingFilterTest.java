package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.NullSymlinkResolver;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class RenameExistingFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        final RenameExistingFilter f = new RenameExistingFilter(new NullSymlinkResolver(), new NullSession(new Host("h")));
        final Path t = new Path("t", Path.FILE_TYPE);
        t.setLocal(new NullLocal("/Downloads", "n"));
        assertTrue(f.accept(t, new TransferStatus().exists(true)));
    }

    @Test
    public void testPrepare() throws Exception {
        final AtomicBoolean c = new AtomicBoolean();
        final RenameExistingFilter f = new RenameExistingFilter(new NullSymlinkResolver(), new NullSession(new Host("h")) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                if(type == Move.class) {
                    return (T) new Move() {
                        @Override
                        public void move(final Path file, final Path renamed) throws BackgroundException {
                            assertNotSame(file.getName(), renamed.getName());
                            c.set(true);
                        }

                        @Override
                        public boolean isSupported(final Path file) {
                            return true;
                        }
                    };
                }
                return super.getFeature(type);
            }

            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> l = new AttributedList<Path>();
                l.add(new Path("t", Path.FILE_TYPE));
                return l;
            }
        });
        final Path p = new Path("t", Path.FILE_TYPE) {
            @Override
            public Path getParent() {
                return new Path("p", Path.DIRECTORY_TYPE);
            }
        };
        p.setLocal(new NullLocal("/Downloads", "n"));
        f.prepare(p, new TransferStatus().exists(true));
        assertFalse(c.get());
        f.apply(p, new TransferStatus().exists(true));
        assertTrue(c.get());
    }

    @Test
    public void testTemporaryFileUpload() throws Exception {
        final Path file = new Path("/t", Path.FILE_TYPE);
        file.setLocal(new NullLocal(null, "a"));
        final AtomicBoolean found = new AtomicBoolean();
        final AtomicInteger moved = new AtomicInteger();
        final NullSession session = new NullSession(new Host("h")) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                if(type.equals(Find.class)) {
                    return (T) new Find() {
                        @Override
                        public boolean find(final Path f) throws BackgroundException {
                            if(f.equals(file)) {
                                found.set(true);
                                return true;
                            }
                            return false;
                        }
                    };
                }
                if(type.equals(Move.class)) {
                    return (T) new Move() {
                        @Override
                        public void move(final Path f, final Path renamed) throws BackgroundException {
                            if(moved.incrementAndGet() == 1) {
                                assertEquals(file, f);
                            }
                            else if(moved.get() == 2) {
                                assertEquals(file, renamed);
                            }
                            else {
                                fail();
                            }
                        }

                        @Override
                        public boolean isSupported(final Path file) {
                            return true;
                        }
                    };
                }
                return null;
            }
        };
        final RenameExistingFilter f = new RenameExistingFilter(new NullSymlinkResolver(), session,
                new UploadFilterOptions().withTemporary(true));
        final TransferStatus status = f.prepare(file, new TransferStatus().exists(true));
        assertNotNull(status.getRenamed());
        assertTrue(status.isRename());
        assertNotEquals(file, status.getRenamed());
        assertNotNull(status.getRenamed().getLocal());
        assertEquals(new NullLocal(null, "a"), status.getRenamed().getLocal());
        // Complete
        status.setLength(1L);
        status.setCurrent(1L);
        f.complete(file, new TransferOptions(), status, new DisabledProgressListener());
        assertTrue(found.get());
        assertEquals(2, moved.get());
    }

    @Test
    public void testTemporaryDirectoryUpload() throws Exception {
        final Path file = new Path("/t", Path.DIRECTORY_TYPE);
        file.setLocal(new NullLocal(null, "a"));
        final AtomicBoolean found = new AtomicBoolean();
        final AtomicBoolean moved = new AtomicBoolean();
        final NullSession session = new NullSession(new Host("h")) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                if(type.equals(Find.class)) {
                    return (T) new Find() {
                        @Override
                        public boolean find(final Path f) throws BackgroundException {
                            if(f.equals(file)) {
                                found.set(true);
                                return true;
                            }
                            return false;
                        }
                    };
                }
                if(type.equals(Move.class)) {
                    return (T) new Move() {
                        @Override
                        public void move(final Path f, final Path renamed) throws BackgroundException {
                            assertFalse(moved.get());
                            assertEquals(file, f);
                            moved.set(true);
                        }

                        @Override
                        public boolean isSupported(final Path file) {
                            return true;
                        }
                    };
                }
                return null;
            }
        };
        final RenameExistingFilter f = new RenameExistingFilter(new NullSymlinkResolver(), session,
                new UploadFilterOptions().withTemporary(true));
        final TransferStatus status = f.prepare(file, new TransferStatus().exists(true));
        assertTrue(found.get());
        assertNull(status.getRenamed());
        assertFalse(moved.get());
        f.apply(file, new TransferStatus().exists(true));
        assertTrue(moved.get());
    }
}
