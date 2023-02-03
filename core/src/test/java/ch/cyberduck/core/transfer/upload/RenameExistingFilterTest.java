package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.DisabledUploadSymlinkResolver;

import org.junit.Test;

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
            public <T> T _getFeature(final Class<T> type) {
                if(type == Move.class) {
                    return (T) new Move() {
                        @Override
                        public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) {
                            assertNotSame(file.getName(), renamed.getName());
                            c.set(true);
                            return renamed;
                        }

                        @Override
                        public boolean isRecursive(final Path source, final Path target) {
                            return true;
                        }

                    };
                }
                return super._getFeature(type);
            }

            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> l = new AttributedList<>();
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
        f.prepare(p, new NullLocal(System.getProperty("java.io.tmpdir"), "t"), new TransferStatus().exists(true), new DisabledProgressListener());
        assertFalse(c.get());
        f.apply(p, new NullLocal(System.getProperty("java.io.tmpdir"), "t"), new TransferStatus().exists(true), new DisabledProgressListener());
        assertTrue(c.get());
    }

    @Test
    public void testFileUploadWithTemporaryFilename() throws Exception {
        final Path file = new Path("/t", EnumSet.of(Path.Type.file));
        final AtomicBoolean found = new AtomicBoolean();
        final AtomicInteger moved = new AtomicInteger();
        final Find find = new Find() {
            @Override
            public boolean find(final Path f, final ListProgressListener listener) {
                if(f.equals(file)) {
                    found.set(true);
                    return true;
                }
                return false;
            }
        };
        final AttributesFinder attributes = new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file, final ListProgressListener listener) {
                return new PathAttributes();
            }
        };
        final Host host = new Host(new TestProtocol());
        final NullSession session = new NullSession(host) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type.equals(Move.class)) {
                    return (T) new Move() {
                        @Override
                        public Path move(final Path source, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) {
                            if(moved.incrementAndGet() == 1) {
                                // Rename existing target file
                                assertEquals(file, source);
                            }
                            else if(moved.get() == 2) {
                                // Move temporary renamed file in place
                                assertEquals(file, renamed);
                            }
                            else {
                                fail();
                            }
                            return renamed;
                        }

                        @Override
                        public boolean isRecursive(final Path source, final Path target) {
                            return true;
                        }

                    };
                }
                if(type.equals(Write.class)) {
                    return (T) new Write<Void>() {
                        @Override
                        public StatusOutputStream write(final Path file, final TransferStatus status, final ConnectionCallback callback) {
                            fail();
                            return null;
                        }

                        @Override
                        public Append append(final Path file, final TransferStatus status) {
                            fail();
                            return new Append(false);
                        }
                    };
                }
                return null;
            }
        };
        final UploadFilterOptions options = new UploadFilterOptions(host).withTemporary(true);
        final RenameExistingFilter f = new RenameExistingFilter(new DisabledUploadSymlinkResolver(), session,
            options);
        f.withFinder(find).withAttributes(attributes);
        assertTrue(options.temporary);
        final TransferStatus status = f.prepare(file, new NullLocal("t"), new TransferStatus().exists(true), new DisabledProgressListener());
        f.apply(file, new NullLocal("t"), status, new DisabledProgressListener());
        assertFalse(status.isExists());
        assertFalse(status.getDisplayname().exists);
        assertNotNull(status.getRename());
        assertNotNull(status.getRename().remote);
        assertEquals(file, status.getDisplayname().remote);
        assertNull(status.getRename().local);
        f.apply(file, new NullLocal("t"), status, new DisabledProgressListener());
        // Complete
        status.setComplete();
        f.complete(file, new NullLocal("t"), status, new DisabledProgressListener());
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
            public boolean find(final Path f, final ListProgressListener listener) {
                if(f.equals(file)) {
                    found.set(true);
                    return true;
                }
                return false;
            }
        };
        final AttributesFinder attributes = new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file, final ListProgressListener listener) {
                return new PathAttributes();
            }
        };
        final Host host = new Host(new TestProtocol());
        final NullSession session = new NullSession(host) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type.equals(Move.class)) {
                    return (T) new Move() {
                        @Override
                        public Path move(final Path f, final Path renamed, TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) {
                            assertFalse(moved.get());
                            assertEquals(file, f);
                            moved.set(true);
                            return renamed;
                        }

                        @Override
                        public boolean isRecursive(final Path source, final Path target) {
                            return true;
                        }

                    };
                }
                if(type.equals(Write.class)) {
                    return (T) new Write<Void>() {
                        @Override
                        public StatusOutputStream write(final Path file, final TransferStatus status, final ConnectionCallback callback) {
                            fail();
                            return null;
                        }

                        @Override
                        public Append append(final Path file, final TransferStatus status) {
                            fail();
                            return new Append(false);
                        }
                    };
                }
                return null;
            }
        };
        final RenameExistingFilter f = new RenameExistingFilter(new DisabledUploadSymlinkResolver(), session,
            new UploadFilterOptions(host).withTemporary(true));
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
        }, new TransferStatus().exists(true), new DisabledProgressListener());
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
