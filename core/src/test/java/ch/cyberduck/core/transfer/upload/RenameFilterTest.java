package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.DisabledUploadSymlinkResolver;

import org.junit.Test;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class RenameFilterTest {

    @Test
    public void testPrepare() throws Exception {
        RenameFilter f = new RenameFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol())));
        final Path t = new Path("t", EnumSet.of(Path.Type.file));
        f.prepare(t, new NullLocal("t"), new TransferStatus(), new DisabledProgressListener());
        assertNotSame("t", t.getName());
    }

    @Test
    public void testFileUploadWithTemporaryFilename() throws Exception {
        final Path file = new Path("f", EnumSet.of(Path.Type.file));
        final AtomicBoolean found = new AtomicBoolean();
        final AttributesFinder attributes = new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file, final ListProgressListener listener) {
                return new PathAttributes();
            }
        };
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
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        final RenameFilter f = new RenameFilter(new DisabledUploadSymlinkResolver(), session,
                find, attributes,
                new UploadFilterOptions(session.getHost()).withTemporary(true));
        final TransferStatus status = f.prepare(file, new NullLocal("t/f"), new TransferStatus().exists(true), new DisabledProgressListener());
        assertTrue(found.get());
        assertFalse(status.isExists());
        f.apply(file, new NullLocal("t/f"), status, new DisabledProgressListener());
        assertNotNull(status.getDisplayname().remote);
        assertNotEquals(file, status.getRename().remote);
        assertFalse(status.isExists());
        assertNotEquals(file, status.getDisplayname().remote);
        assertFalse(status.getDisplayname().exists);
        assertNull(status.getRename().local);
        assertNotNull(status.getRename().remote);
        // assertEquals(new Path("/f-2g3vYDqR-", EnumSet.of(Path.Type.file)), fileStatus.getRename().remote);
        assertEquals(new Path("/f-1", EnumSet.of(Path.Type.file)), status.getDisplayname().remote);
        assertNotEquals(status.getDisplayname().remote, status.getRename().remote);
    }

    @Test
    public void testDirectoryUploadRename() throws Exception {
        final Path directory = new Path("/t", EnumSet.of(Path.Type.directory));
        final Path file = new Path(directory, "f", EnumSet.of(Path.Type.file));
        final AtomicBoolean found = new AtomicBoolean();
        final AttributesFinder attributes = new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file, final ListProgressListener listener) {
                return new PathAttributes();
            }
        };
        final Find find = new Find() {
            @Override
            public boolean find(final Path f, final ListProgressListener listener) {
                if(f.equals(directory)) {
                    found.set(true);
                    return true;
                }
                return false;
            }
        };
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        final RenameFilter f = new RenameFilter(new DisabledUploadSymlinkResolver(), session, find, attributes);
        final TransferStatus directoryStatus = f.prepare(directory, new NullLocal("t"), new TransferStatus().exists(true), new DisabledProgressListener());
        assertTrue(found.get());
        assertFalse(directoryStatus.isExists());
        f.apply(directory, new NullLocal("t"), directoryStatus, new DisabledProgressListener());
        assertNotNull(directoryStatus.getRename());
        assertNotEquals(directory, directoryStatus.getRename().remote);
        assertEquals(new Path("/t-1", EnumSet.of(Path.Type.directory)), directoryStatus.getRename().remote);
        assertNull(directoryStatus.getDisplayname().remote);
        assertNull(directoryStatus.getRename().local);
        assertNotNull(directoryStatus.getRename().remote);
        final TransferStatus fileStatus = f.prepare(file, new NullLocal("t/f"), directoryStatus, new DisabledProgressListener());
        f.apply(file, new NullLocal("t/f"), fileStatus, new DisabledProgressListener());
        assertFalse(fileStatus.isExists());
        assertNotNull(fileStatus.getRename());
        assertEquals(new Path(new Path("/t-1", EnumSet.of(Path.Type.directory)), "f", EnumSet.of(Path.Type.file)), fileStatus.getRename().remote);
        assertNull(fileStatus.getDisplayname().remote);
        assertNull(fileStatus.getRename().local);
        assertNotNull(fileStatus.getRename().remote);
        assertEquals(new Path("/t-1/f", EnumSet.of(Path.Type.file)), fileStatus.getRename().remote);
    }
}
