package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.NullUploadFeature;
import ch.cyberduck.core.NullWriteFeature;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.DisabledUploadSymlinkResolver;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class ResumeFilterTest {

    @Test
    public void testAccept() throws Exception {
        final ResumeFilter f = new ResumeFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol())));
        assertTrue(f.accept(new Path("t", EnumSet.of(Path.Type.file)), new NullLocal("a") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public LocalAttributes attributes() {
                return new LocalAttributes(this.getAbsolute()) {
                    @Override
                    public long getSize() {
                        return 1L;
                    }
                };
            }
        }, new TransferStatus().exists(true)));
    }

    @Test
    public void testSkip() throws Exception {
        final Path file = new Path("t", EnumSet.of(Path.Type.file));
        final ResumeFilter f = new ResumeFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Find.class) {
                    return (T) new Find() {
                        @Override
                        public boolean find(final Path file, final ListProgressListener listener) {
                            return true;
                        }
                    };
                }
                return super._getFeature(type);
            }

            @Override
            public AttributedList<Path> list(final Path parent, final ListProgressListener listener) {
                return new AttributedList<>(Collections.singletonList(file));
            }
        });
        file.attributes().setSize(1L);
        assertFalse(f.accept(file, new NullLocal("a") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public LocalAttributes attributes() {
                return new LocalAttributes("t") {
                    @Override
                    public long getSize() {
                        return 1L;
                    }
                };
            }

        }, new TransferStatus().exists(true)));
    }

    @Test
    public void testPrepareNoAppend() throws Exception {
        final Host host = new Host(new TestProtocol());
        final ResumeFilter f = new ResumeFilter(new DisabledUploadSymlinkResolver(), new NullSession(host),
            new UploadFilterOptions(host).withTemporary(true));
        final Path t = new Path("t", EnumSet.of(Path.Type.file));
        t.attributes().setSize(7L);
        final TransferStatus status = f.prepare(t, new NullLocal("t"), new TransferStatus().exists(true), new DisabledProgressListener());
        assertFalse(status.isAppend());
        assertFalse(status.isExists());
        assertNotNull(status.getRename().remote);
        assertNotEquals(t, status.getRename().remote);
    }

    @Test
    public void testPrepareAppend() throws Exception {
        final Host host = new Host(new TestProtocol());
        final ResumeFilter f = new ResumeFilter(new DisabledUploadSymlinkResolver(), new NullSession(host) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final Path f = new Path("t", EnumSet.of(Path.Type.file));
                f.attributes().setSize(7L);
                return new AttributedList<>(Collections.singletonList(f));
            }
        }, new UploadFilterOptions(host).withTemporary(true));
        final Path t = new Path("t", EnumSet.of(Path.Type.file));
        final TransferStatus status = f.prepare(t, new NullLocal("t") {
            @Override
            public LocalAttributes attributes() {
                return new LocalAttributes("t") {
                    @Override
                    public long getSize() {
                        return 8L;
                    }
                };
            }

            @Override
            public boolean isFile() {
                return true;
            }
        }, new TransferStatus().exists(true), new DisabledProgressListener());
        assertTrue(status.isAppend());
        assertTrue(status.isExists());
        // Temporary target
        assertNull(status.getRename().remote);
        assertEquals(7L, status.getOffset());
    }

    @Test
    public void testAppendEqualSize() throws Exception {
        final Host host = new Host(new TestProtocol());
        final NullSession session = new NullSession(host) {
            @Override
            public AttributedList<Path> list(final Path folder, final ListProgressListener listener) throws BackgroundException {
                final AttributedList<Path> list = new AttributedList<>(Collections.singletonList(new Path(folder, "t", EnumSet.of(Path.Type.file))
                        .withAttributes(new PathAttributes().withSize(3L))));
                listener.chunk(folder, list);
                return list;
            }
        };
        final ResumeFilter f = new ResumeFilter(new DisabledUploadSymlinkResolver(), session,
                new UploadFilterOptions(host).withTemporary(true), new NullUploadFeature());
        final long size = 3L;
        final Path t = new Path("t", EnumSet.of(Path.Type.file));
        assertFalse(f.accept(t, new NullLocal("t") {
            @Override
            public LocalAttributes attributes() {
                return new LocalAttributes("t") {
                    @Override
                    public long getSize() {
                        return size;
                    }
                };
            }

            @Override
            public boolean isFile() {
                return true;
            }

            @Override
            public boolean exists() {
                return true;
            }
        }, new TransferStatus().exists(true)));
    }

    @Test
    public void testAppendSmallerSize() throws Exception {
        final Host host = new Host(new TestProtocol());
        final NullSession session = new NullSession(host) {
            @Override
            public AttributedList<Path> list(final Path folder, final ListProgressListener listener) throws BackgroundException {
                final AttributedList<Path> list = new AttributedList<>(Collections.singletonList(new Path(folder, "t", EnumSet.of(Path.Type.file))
                    .withAttributes(new PathAttributes().withSize(2L))));
                listener.chunk(folder, list);
                return list;
            }
        };
        final ResumeFilter f = new ResumeFilter(new DisabledUploadSymlinkResolver(), session,
                new UploadFilterOptions(host).withTemporary(true), new NullUploadFeature());
        final long size = 3L;
        final Path t = new Path("t", EnumSet.of(Path.Type.file));
        final NullLocal l = new NullLocal("t") {
            @Override
            public LocalAttributes attributes() {
                return new LocalAttributes("t") {
                    @Override
                    public long getSize() {
                        return size;
                    }
                };
            }

            @Override
            public boolean isFile() {
                return true;
            }

            @Override
            public boolean exists() {
                return true;
            }
        };
        assertTrue(f.accept(t, l, new TransferStatus().exists(true)));
        // Remaining length to transfer is 1
        assertEquals(1L, f.prepare(t, l, new TransferStatus().exists(true), new DisabledProgressListener()).getLength());
        // Skip first 2 bytes
        assertEquals(2L, f.prepare(t, l, new TransferStatus().exists(true), new DisabledProgressListener()).getOffset());
    }

    @Test
    public void testAppendLargerSize() throws Exception {
        final Host host = new Host(new TestProtocol());
        final NullSession session = new NullSession(host) {
            @Override
            public AttributedList<Path> list(final Path folder, final ListProgressListener listener) throws BackgroundException {
                final AttributedList<Path> list = new AttributedList<>(Collections.singletonList(new Path(folder, "t", EnumSet.of(Path.Type.file))
                    .withAttributes(new PathAttributes().withSize(4L))));
                listener.chunk(folder, list);
                return list;
            }
        };
        final ResumeFilter f = new ResumeFilter(new DisabledUploadSymlinkResolver(), session,
                new UploadFilterOptions(host).withTemporary(true), new DefaultUploadFeature<>(new NullWriteFeature()));
        final long size = 3L;
        final Path t = new Path("t", EnumSet.of(Path.Type.file));
        final NullLocal l = new NullLocal("t") {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public LocalAttributes attributes() {
                return new LocalAttributes("t") {
                    @Override
                    public long getSize() {
                        return size;
                    }
                };
            }

            @Override
            public boolean isFile() {
                return true;
            }
        };
        assertTrue(f.accept(t, l, new TransferStatus().exists(true)));
        assertFalse(f.prepare(t, l, new TransferStatus().exists(true), new DisabledProgressListener()).isAppend());
    }
}
