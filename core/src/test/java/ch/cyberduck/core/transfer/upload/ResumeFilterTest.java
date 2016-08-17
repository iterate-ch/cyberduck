package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.shared.DefaultAttributesFeature;
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
            public <T> T getFeature(final Class<T> type) {
                if(type == Find.class) {
                    return (T) new Find() {
                        @Override
                        public boolean find(final Path file) throws BackgroundException {
                            return true;
                        }

                        @Override
                        public Find withCache(PathCache cache) {
                            return this;
                        }
                    };
                }
                return super.getFeature(type);
            }

            @Override
            public AttributedList<Path> list(final Path parent, final ListProgressListener listener) {
                return new AttributedList<Path>(Collections.singletonList(file));
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
    public void testPrepareFalse() throws Exception {
        final ResumeFilter f = new ResumeFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol())),
                new UploadFilterOptions().withTemporary(true));
        final Path t = new Path("t", EnumSet.of(Path.Type.file));
        t.attributes().setSize(7L);
        final TransferStatus status = f.prepare(t, new NullLocal("t"), new TransferStatus().exists(true));
        assertFalse(status.isAppend());
        assertFalse(status.isRename());
    }

    @Test
    public void testPrepare() throws Exception {
        final ResumeFilter f = new ResumeFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final Path f = new Path("t", EnumSet.of(Path.Type.file));
                f.attributes().setSize(7L);
                return new AttributedList<Path>(Collections.<Path>singletonList(f));
            }

            @Override
            public <T> T getFeature(Class<T> type) {
                if(type == Attributes.class) {
                    return (T) new DefaultAttributesFeature(this);
                }
                return super.getFeature(type);
            }
        }, new UploadFilterOptions().withTemporary(true));
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
        }, new TransferStatus().exists(true));
        assertTrue(status.isAppend());
        assertFalse(status.isRename());
        assertEquals(7L, status.getOffset());
    }

    @Test
    public void testPrepare0() throws Exception {
        final ResumeFilter f = new ResumeFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol())),
                new UploadFilterOptions().withTemporary(true));
        final Path t = new Path("t", EnumSet.of(Path.Type.file));
        t.attributes().setSize(0L);
        final TransferStatus status = f.prepare(t, new NullLocal("t"), new TransferStatus().exists(true));
        assertFalse(status.isAppend());
        assertFalse(status.isRename());
        assertEquals(0L, status.getOffset());
    }

    @Test
    public void testAppendEqualSize() throws Exception {
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        final ResumeFilter f = new ResumeFilter(new DisabledUploadSymlinkResolver(), session,
                new UploadFilterOptions().withTemporary(true), new DefaultUploadFeature(session) {
            @Override
            public Write.Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
                return new Write.Append(length);
            }
        });
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
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        final ResumeFilter f = new ResumeFilter(new DisabledUploadSymlinkResolver(), session,
                new UploadFilterOptions().withTemporary(true), new DefaultUploadFeature(session) {
            @Override
            public Write.Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
                return new Write.Append(length - 1);
            }
        });
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
        assertEquals(1L, f.prepare(t, l, new TransferStatus().exists(true)).getLength());
        // Skip first 2 bytes
        assertEquals(2L, f.prepare(t, l, new TransferStatus().exists(true)).getOffset());
    }

    @Test
    public void testAppendLargerSize() throws Exception {
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        final ResumeFilter f = new ResumeFilter(new DisabledUploadSymlinkResolver(), session,
                new UploadFilterOptions().withTemporary(true), new DefaultUploadFeature(session) {
            @Override
            public Write.Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
                return new Write.Append(length + 1);
            }
        });
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
        assertFalse(f.prepare(t, l, new TransferStatus().exists(true)).isAppend());
    }
}