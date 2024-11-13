package ch.cyberduck.core.transfer.copy;

import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.NullTransferSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.shared.DefaultTimestampFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.upload.UploadFilterOptions;

import org.junit.Test;

import java.util.EnumSet;
import java.util.HashMap;

import static org.junit.Assert.*;

public class OverwriteFilterTest {

    @Test
    public void testAcceptDirectoryNew() throws Exception {
        final HashMap<Path, Path> files = new HashMap<>();
        final Path source = new Path("a", EnumSet.of(Path.Type.directory));
        files.put(source, new Path("a", EnumSet.of(Path.Type.directory)));
        AbstractCopyFilter f = new OverwriteFilter(new NullSession(new Host(new TestProtocol())),
                new NullSession(new Host(new TestProtocol())), files);
        assertTrue(f.accept(source, null, new TransferStatus(), new DisabledProgressListener()));
    }

    @Test
    public void testAcceptDirectoryExists() throws Exception {
        final HashMap<Path, Path> files = new HashMap<>();
        final Path source = new Path("a", EnumSet.of(Path.Type.directory));
        files.put(source, new Path("a", EnumSet.of(Path.Type.directory)));
        final Find find = new Find() {
            @Override
            public boolean find(final Path file, final ListProgressListener listener) throws BackgroundException {
                return true;
            }
        };
        AbstractCopyFilter f = new OverwriteFilter(new NullTransferSession(new Host(new TestProtocol())), new NullTransferSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type.equals(Find.class)) {
                    return (T) find;
                }
                return super._getFeature(type);
            }
        }, files);
        assertTrue(f.accept(source, null, new TransferStatus().exists(true), new DisabledProgressListener()));
        final TransferStatus status = f.prepare(source, null, new TransferStatus().exists(true), new DisabledProgressListener());
        assertTrue(status.isExists());
    }

    @Test
    public void testPrepareFile() throws Exception {
        final HashMap<Path, Path> files = new HashMap<>();
        final Path source = new Path("a", EnumSet.of(Path.Type.file));
        source.attributes().setSize(1L);
        files.put(source, new Path("a", EnumSet.of(Path.Type.file)));
        OverwriteFilter f = new OverwriteFilter(new NullTransferSession(new Host(new TestProtocol())), new NullSession(new Host(new TestProtocol())), files);
        final TransferStatus status = f.prepare(source, null, new TransferStatus(), new DisabledProgressListener());
        assertEquals(1L, status.getLength());
    }

    @Test
    public void testPrepareDirectory() throws Exception {
        final HashMap<Path, Path> files = new HashMap<>();
        final Path source = new Path("a", EnumSet.of(Path.Type.directory));
        source.attributes().setSize(1L);
        final Path target = new Path("a", EnumSet.of(Path.Type.directory));
        files.put(source, target);
        OverwriteFilter f = new OverwriteFilter(new NullTransferSession(new Host(new TestProtocol())), new NullSession(new Host(new TestProtocol())), files);
        final TransferStatus status = f.prepare(source, null, new TransferStatus(), new DisabledProgressListener());
        assertEquals(0L, status.getLength());
    }

    @Test
    public void testComplete() throws Exception {
        final HashMap<Path, Path> files = new HashMap<>();
        final Path source = new Path("a", EnumSet.of(Path.Type.file));
        source.attributes().setSize(1L);
        source.attributes().setPermission(new Permission(777));
        final Long time = System.currentTimeMillis();
        source.attributes().setModificationDate(time);
        final boolean[] timestampWrite = new boolean[1];
        final boolean[] permissionWrite = new boolean[1];
        final Path target = new Path("a", EnumSet.of(Path.Type.file));
        files.put(source, target);
        final Host host = new Host(new TestProtocol());
        OverwriteFilter f = new OverwriteFilter(new NullTransferSession(host), new NullSession(host) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type.equals(Timestamp.class)) {
                    return (T) new DefaultTimestampFeature() {

                        @Override
                        public void setTimestamp(final Path file, final TransferStatus status) {
                            assertEquals(time, status.getModified());
                            timestampWrite[0] = true;
                        }
                    };
                }
                if(type.equals(UnixPermission.class)) {
                    return (T) new UnixPermission() {
                        @Override
                        public void setUnixOwner(final Path file, final String owner) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setUnixGroup(final Path file, final String group) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public Permission getUnixPermission(final Path file) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setUnixPermission(final Path file, final TransferStatus status) {
                            assertEquals(new Permission(777), status.getPermission());
                            permissionWrite[0] = true;
                        }
                    };
                }
                return super._getFeature(type);
            }
        }, files, new UploadFilterOptions(host).withPermission(true).withTimestamp(true));
        final TransferStatus status = f.prepare(source, null, new TransferStatus(), new DisabledProgressListener());
        f.complete(source, null, status, new DisabledProgressListener());
        assertFalse(permissionWrite[0]);
        assertFalse(timestampWrite[0]);
        status.setComplete();
        f.complete(source, null, status, new DisabledProgressListener());
        assertTrue(permissionWrite[0]);
        assertTrue(timestampWrite[0]);
    }
}
