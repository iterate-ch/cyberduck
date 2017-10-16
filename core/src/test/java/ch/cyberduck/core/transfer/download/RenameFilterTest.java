package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.DisabledDownloadSymlinkResolver;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RenameFilterTest {

    @Test
    public void testPrepare() throws Exception {
        RenameFilter f = new RenameFilter(new DisabledDownloadSymlinkResolver(), new NullSession(new Host(new TestProtocol())));
        final String name = new AsciiRandomStringService().random();
        final NullLocal local = new NullLocal("/tmp", name) {
            @Override
            public boolean exists() {
                return name.equals(this.getName());
            }
        };
        final Path t = new Path(name, EnumSet.of(Path.Type.file));
        final TransferStatus status = f.prepare(t, local, new TransferStatus().exists(true), new DisabledProgressListener());
        assertNotNull(status.getRename().local);
        assertEquals(String.format("%s-1", name), status.getRename().local.getName());
    }

    @Test
    public void testDirectoryDownload() throws Exception {
        RenameFilter f = new RenameFilter(new DisabledDownloadSymlinkResolver(), new NullSession(new Host(new TestProtocol())));
        final String name = new AsciiRandomStringService().random();
        final NullLocal local = new NullLocal("/tmp", name) {
            @Override
            public boolean exists() {
                return name.equals(this.getName());
            }

            @Override
            public boolean isFile() {
                return false;
            }

            @Override
            public boolean isDirectory() {
                return true;
            }
        };
        final Path directory = new Path("t", EnumSet.of(Path.Type.directory));
        final Path file = new Path(directory, name, EnumSet.of(Path.Type.file));
        final TransferStatus directoryStatus = f.prepare(directory, local, new TransferStatus().exists(true), new DisabledProgressListener());
        final TransferStatus fileStatus = f.prepare(file, new NullLocal(local, "f"), directoryStatus, new DisabledProgressListener());
        assertNotNull(fileStatus.getRename().local);
        assertEquals(String.format("/tmp/t-1/%s", name), fileStatus.getRename().local.getAbsolute());
    }
}
