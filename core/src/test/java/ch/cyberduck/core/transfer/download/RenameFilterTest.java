package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.DisabledDownloadSymlinkResolver;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertNotSame;

public class RenameFilterTest {

    @Test
    public void testPrepare() throws Exception {
        RenameFilter f = new RenameFilter(new DisabledDownloadSymlinkResolver(), new NullSession(new Host(new TestProtocol())));
        final NullLocal local = new NullLocal("t") {
            @Override
            public boolean exists() {
                return this.getName().equals("/t");
            }
        };
        final Path t = new Path("t", EnumSet.of(Path.Type.file));
        f.prepare(t, local, new TransferStatus());
        assertNotSame("/t", t.getName());
    }
}