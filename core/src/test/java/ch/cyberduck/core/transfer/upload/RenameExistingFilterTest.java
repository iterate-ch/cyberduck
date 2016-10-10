package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.DisabledUploadSymlinkResolver;

import org.junit.Test;

import java.io.OutputStream;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class RenameExistingFilterTest {

    @Test
    public void testAccept() throws Exception {
        final RenameExistingFilter f = new RenameExistingFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol())));
        final Path t = new Path("t", EnumSet.of(Path.Type.file));
        assertTrue(f.accept(t, new NullLocal(System.getProperty("java.io.tmpdir"), "t") {

            @Override
            public boolean exists() {
                return true;
            }

        }, new TransferStatus().exists(true)));
    }

    @Test
    public void testPrepare() throws Exception {
        final AtomicBoolean c = new AtomicBoolean();
        final RenameExistingFilter f = new RenameExistingFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getFeature(final Class<T> type) {
                if(type == Move.class) {
                    return (T) new Move() {
                        @Override
                        public void move(final Path file, final Path renamed, boolean exists, final Delete.Callback callback) throws BackgroundException {
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
                l.add(new Path("t", EnumSet.of(Path.Type.file)));
                return l;
            }
        });
        final Path p = new Path("t", EnumSet.of(Path.Type.file)) {
            @Override
            public Path getParent() {
                return new Path("p", EnumSet.of(Path.Type.directory));
            }
        };
        f.prepare(p, new NullLocal(System.getProperty("java.io.tmpdir"), "t"), new TransferStatus().exists(true));
        assertFalse(c.get());
        f.apply(p, new NullLocal(System.getProperty("java.io.tmpdir"), "t"), new TransferStatus().exists(true), new DisabledProgressListener());
        assertTrue(c.get());
    }

    @Test
    public void testTemporaryFileUpload() throws Exception {
        final Path file = new Path("/t", EnumSet.of(Path.Type.file));
        final AtomicBoolean found = new AtomicBoolean();
        final AtomicInteger moved = new AtomicInteger();
        final Find find = new Find() {
            @Override
            public boolean find(final Path f) throws BackgroundException {
                if(f.equals(file)) {
                    found.set(true);
                    return true;
                }
                return false;
            }

            @Override
            public Find withCache(PathCache cache) {
                return this;
            }
        };
        final Attributes attributes = new Attributes() {
            @Override
            public PathAttributes find(final Path file) throws BackgroundException {
                return new PathAttributes();
            }

            @Override
            public Attributes withCache(PathCache cache) {
                return this;
            }
        };
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getFeature(final Class<T> type) {
                if(type.equals(Move.class)) {
                    return (T) new Move() {
                        @Override
                        public void move(final Path source, final Path target, boolean exists, final Delete.Callback callback) throws BackgroundException {
                            if(moved.incrementAndGet() == 1) {
                                // Rename existing target file
                                assertEquals(file, source);
                            }
                            else if(moved.get() == 2) {
                                // Move temporary renamed file in place
                                assertEquals(file, target);
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
                if(type.equals(Write.class)) {
                    return (T) new Write() {
                        @Override
                        public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
                            fail();
                            return null;
                        }

                        @Override
                        public Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
                            fail();
                            return new Append(1L);
                        }

                        @Override
                        public boolean temporary() {
                            return true;
                        }

                        @Override
                        public boolean random() {
                            return false;
                        }
                    };
                }
                return null;
            }
        };
        final UploadFilterOptions options = new UploadFilterOptions().withTemporary(true);
        final RenameExistingFilter f = new RenameExistingFilter(new DisabledUploadSymlinkResolver(), session,
                options);
        f.withFinder(find).withAttributes(attributes);
        assertTrue(options.temporary);
        final TransferStatus status = f.prepare(file, new NullLocal("t"), new TransferStatus().exists(true));
        assertNotNull(status.getTemporary());
        assertNotNull(status.getTemporary().remote);
        assertNotEquals(file, status.getTemporary().local);
        assertNull(status.getRename().local);
        f.apply(file, new NullLocal("t"), status, new DisabledProgressListener());
        // Complete
        status.setComplete();
        f.complete(file, new NullLocal("t"), new TransferOptions(), status, new DisabledProgressListener());
        assertTrue(found.get());
        assertEquals(2, moved.get());
    }

    @Test
    public void testTemporaryDirectoryUpload() throws Exception {
        final Path file = new Path("/t", EnumSet.of(Path.Type.directory));
        final AtomicBoolean found = new AtomicBoolean();
        final AtomicBoolean moved = new AtomicBoolean();
        final Find find = new Find() {
            @Override
            public boolean find(final Path f) throws BackgroundException {
                if(f.equals(file)) {
                    found.set(true);
                    return true;
                }
                return false;
            }

            @Override
            public Find withCache(PathCache cache) {
                return this;
            }
        };
        final Attributes attributes = new Attributes() {
            @Override
            public PathAttributes find(final Path file) throws BackgroundException {
                return new PathAttributes();
            }

            @Override
            public Attributes withCache(PathCache cache) {
                return this;
            }
        };
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getFeature(final Class<T> type) {
                if(type.equals(Move.class)) {
                    return (T) new Move() {
                        @Override
                        public void move(final Path f, final Path renamed, boolean exists, final Delete.Callback callback) throws BackgroundException {
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
                if(type.equals(Write.class)) {
                    return (T) new Write() {
                        @Override
                        public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
                            fail();
                            return null;
                        }

                        @Override
                        public Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
                            fail();
                            return new Append(0L);
                        }

                        @Override
                        public boolean temporary() {
                            return true;
                        }

                        @Override
                        public boolean random() {
                            return false;
                        }
                    };
                }
                return null;
            }
        };
        final RenameExistingFilter f = new RenameExistingFilter(new DisabledUploadSymlinkResolver(), session,
                new UploadFilterOptions().withTemporary(true));
        f.withFinder(find).withAttributes(attributes);
        final TransferStatus status = f.prepare(file, new NullLocal("/t") {
            @Override
            public boolean isDirectory() {
                return true;
            }

            @Override
            public boolean isFile() {
                return false;
            }
        }, new TransferStatus().exists(true));
        assertTrue(found.get());
        assertNull(status.getRename().remote);
        assertNull(status.getRename().local);
        assertFalse(moved.get());
        f.apply(file, new NullLocal("/t") {
            @Override
            public boolean isDirectory() {
                return true;
            }
        }, new TransferStatus().exists(true), new DisabledProgressListener());
        assertTrue(moved.get());
    }
}
