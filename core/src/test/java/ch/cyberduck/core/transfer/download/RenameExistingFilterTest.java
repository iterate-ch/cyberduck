package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.DisabledDownloadSymlinkResolver;

import org.junit.Test;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class RenameExistingFilterTest {

    @Test
    public void testPrepare() throws Exception {
        final DownloadFilterOptions options = new DownloadFilterOptions();
        options.icon = false;
        RenameExistingFilter f = new RenameExistingFilter(new DisabledDownloadSymlinkResolver(), new NullSession(new Host(new TestProtocol())),
                options);
        final NullLocal local = new NullLocal(System.getProperty("java.io.tmpdir"), "t-1") {
            @Override
            public boolean exists() {
                return false;
            }

            @Override
            public void rename(final Local renamed) {
                fail();
            }
        };
        final Path p = new Path("t-1", EnumSet.of(Path.Type.file));
        final TransferStatus status = f.prepare(p, local, new TransferStatus());
        assertNull(status.getRename().local);
        f.apply(p, local, new TransferStatus(), new DisabledProgressListener());
    }

    @Test
    public void testPrepareRename() throws Exception {
        final AtomicBoolean r = new AtomicBoolean();
        RenameExistingFilter f = new RenameExistingFilter(new DisabledDownloadSymlinkResolver(), new NullSession(new Host(new TestProtocol())));
        final NullLocal local = new NullLocal(System.getProperty("java.io.tmpdir"), "t-2") {
            @Override
            public boolean exists() {
                return "t-2".equals(this.getName());
            }

            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public boolean isFile() {
                return true;
            }

            @Override
            public void rename(final Local renamed) {
                assertTrue(renamed.getName().startsWith("t-2"));
                r.set(true);
            }
        };
        final Path p = new Path("t-2", EnumSet.of(Path.Type.file));
        final TransferStatus status = f.prepare(p, local, new TransferStatus().exists(true));
        assertNull(status.getRename().local);
        assertFalse(r.get());
        f.apply(p, local, status, new DisabledProgressListener());
        assertEquals("t-2", local.getName());
        assertTrue(r.get());
    }
}