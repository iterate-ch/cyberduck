package ch.cyberduck.core.transfer.download;

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
        final NullLocal local = new NullLocal("/tmp/t") {
            @Override
            public boolean exists() {
                return "t".equals(this.getName());
            }
        };
        final Path t = new Path("t", EnumSet.of(Path.Type.file));
        final TransferStatus status = f.prepare(t, local, new TransferStatus().exists(true), new DisabledProgressListener());
        assertNotNull(status.getRename().local);
        assertEquals(status.getRename().local.getName(), "t-1");
    }
}
