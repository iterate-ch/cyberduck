package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullTransferSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.local.DefaultLocalTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.DisabledDownloadSymlinkResolver;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class RenameExistingFilterTest {

    @Test
    public void testPrepare() throws Exception {
        final Host host = new Host(new TestProtocol());
        final DownloadFilterOptions options = new DownloadFilterOptions(host);
        options.icon = false;
        RenameExistingFilter f = new RenameExistingFilter(new DisabledDownloadSymlinkResolver(), new NullTransferSession(host),
            options);
        final String name = new AsciiRandomStringService().random();
        final Local local = new NullLocal(System.getProperty("java.io.tmpdir"), name) {
            @Override
            public boolean exists() {
                return false;
            }

            @Override
            public void rename(final Local renamed) {
                // Must not rename original file but copy
                fail();
            }
        };
        final Path p = new Path(name, EnumSet.of(Path.Type.file));
        final TransferStatus status = f.prepare(p, local, new TransferStatus(), new DisabledProgressListener());
        assertNull(status.getRename().local);
        assertFalse(status.isExists());
        f.apply(p, local, new TransferStatus(), new DisabledProgressListener());
    }

    @Test
    public void testApplyRename() throws Exception {
        RenameExistingFilter f = new RenameExistingFilter(new DisabledDownloadSymlinkResolver(), new NullTransferSession(new Host(new TestProtocol())));
        final String name = new AsciiRandomStringService().random();
        final Local local = LocalFactory.get(System.getProperty("java.io.tmpdir"), name);
        new DefaultLocalTouchFeature().touch(local);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(RandomUtils.nextBytes(1)),
            local.getOutputStream(false));
        final Path p = new Path(name, EnumSet.of(Path.Type.file));
        final TransferStatus status = f.prepare(p, local, new TransferStatus().exists(true), new DisabledProgressListener());
        assertTrue(status.isExists());
        assertNull(status.getRename().local);
        f.apply(p, local, status, new DisabledProgressListener());
        assertEquals(name, local.getName());
        assertFalse(status.isExists());
    }

    @Test
    public void testPrepareRenameEmptyFile() throws Exception {
        RenameExistingFilter f = new RenameExistingFilter(new DisabledDownloadSymlinkResolver(), new NullTransferSession(new Host(new TestProtocol())));
        final String name = new AsciiRandomStringService().random();
        final Local local = new NullLocal(System.getProperty("java.io.tmpdir"), name) {
            @Override
            public void rename(final Local renamed) {
                // Must not rename original file but copy
                fail();
            }
        };
        new DefaultLocalTouchFeature().touch(local);
        final Path p = new Path(name, EnumSet.of(Path.Type.file));
        final TransferStatus status = f.prepare(p, local, new TransferStatus().exists(true), new DisabledProgressListener());
        assertTrue(status.isExists());
        assertNull(status.getRename().local);
        f.apply(p, local, status, new DisabledProgressListener());
        assertEquals(name, local.getName());
        assertTrue(status.isExists());
        local.delete();
    }
}
