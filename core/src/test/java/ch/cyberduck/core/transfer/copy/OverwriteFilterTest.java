package ch.cyberduck.core.transfer.copy;

import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.shared.DefaultTimestampFeature;
import ch.cyberduck.core.shared.DefaultUnixPermissionFeature;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.upload.UploadFilterOptions;

import org.junit.Test;

import java.util.EnumSet;
import java.util.HashMap;

import static org.junit.Assert.*;

public class OverwriteFilterTest {

    @Test
    public void testAcceptDirectoryNew() throws Exception {
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        final Path source = new Path("a", EnumSet.of(Path.Type.directory));
        files.put(source, new Path("a", EnumSet.of(Path.Type.directory)));
        AbstractCopyFilter f = new OverwriteFilter(new NullSession(new Host(new TestProtocol())),
                new NullSession(new Host(new TestProtocol())), files);
        assertTrue(f.accept(source, null, new TransferStatus()));
    }

    @Test
    public void testAcceptDirectoryExists() throws Exception {
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        final Path source = new Path("a", EnumSet.of(Path.Type.directory));
        files.put(source, new Path("a", EnumSet.of(Path.Type.directory)));
        final Find find = new Find() {
            @Override
            public boolean find(final Path file) throws BackgroundException {
                return true;
            }

            @Override
            public Find withCache(PathCache cache) {
                return this;
            }
        };
        AbstractCopyFilter f = new OverwriteFilter(new NullSession(new Host(new TestProtocol())), new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getFeature(final Class<T> type) {
                if(type.equals(Find.class)) {
                    return (T) find;
                }
                return super.getFeature(type);
            }
        }, files);
        assertTrue(f.accept(source, null, new TransferStatus().exists(true)));
        final TransferStatus status = f.prepare(source, null, new TransferStatus().exists(true));
        assertTrue(status.isExists());
    }

    @Test
    public void testPrepareFile() throws Exception {
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        final Path source = new Path("a", EnumSet.of(Path.Type.file));
        source.attributes().setSize(1L);
        files.put(source, new Path("a", EnumSet.of(Path.Type.file)));
        OverwriteFilter f = new OverwriteFilter(new NullSession(new Host(new TestProtocol())), new NullSession(new Host(new TestProtocol())), files);
        final TransferStatus status = f.prepare(source, null, new TransferStatus());
        assertEquals(1L, status.getLength());
    }

    @Test
    public void testPrepareDirectory() throws Exception {
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        final Path source = new Path("a", EnumSet.of(Path.Type.directory));
        source.attributes().setSize(1L);
        final Path target = new Path("a", EnumSet.of(Path.Type.directory));
        files.put(source, target);
        OverwriteFilter f = new OverwriteFilter(new NullSession(new Host(new TestProtocol())), new NullSession(new Host(new TestProtocol())), files);
        final TransferStatus status = f.prepare(source, null, new TransferStatus());
        assertEquals(0L, status.getLength());
    }

    @Test
    public void testComplete() throws Exception {
        final HashMap<Path, Path> files = new HashMap<Path, Path>();
        final Path source = new Path("a", EnumSet.of(Path.Type.file));
        source.attributes().setSize(1L);
        source.attributes().setPermission(new Permission(777));
        final Long time = System.currentTimeMillis();
        source.attributes().setModificationDate(time);
        final boolean[] timestampWrite = new boolean[1];
        final boolean[] permissionWrite = new boolean[1];
        final Path target = new Path("a", EnumSet.of(Path.Type.file));
        files.put(source, target);
        OverwriteFilter f = new OverwriteFilter(new NullSession(new Host(new TestProtocol())), new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getFeature(final Class<T> type) {
                if(type.equals(Timestamp.class)) {
                    return (T) new DefaultTimestampFeature() {

                        @Override
                        public void setTimestamp(final Path file, final Long modified) throws BackgroundException {
                            assertEquals(time, modified);
                            timestampWrite[0] = true;
                        }
                    };
                }
                if(type.equals(UnixPermission.class)) {
                    return (T) new DefaultUnixPermissionFeature() {
                        @Override
                        public void setUnixOwner(final Path file, final String owner) throws BackgroundException {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setUnixGroup(final Path file, final String group) throws BackgroundException {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setUnixPermission(final Path file, final Permission permission) throws BackgroundException {
                            assertEquals(new Permission(777), permission);
                            permissionWrite[0] = true;
                        }
                    };
                }
                return super.getFeature(type);
            }
        }, files, new UploadFilterOptions().withPermission(true).withTimestamp(true));
        final TransferStatus status = f.prepare(source, null, new TransferStatus());
        f.complete(source, null, new TransferOptions(), status, new DisabledProgressListener());
        assertFalse(permissionWrite[0]);
        assertFalse(timestampWrite[0]);
        status.setComplete();
        f.complete(source, null, new TransferOptions(), status, new DisabledProgressListener());
        assertTrue(permissionWrite[0]);
        assertTrue(timestampWrite[0]);
    }
}
