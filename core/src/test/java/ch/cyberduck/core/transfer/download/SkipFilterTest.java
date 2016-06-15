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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SkipFilterTest {

    @Test
    public void testAccept() throws Exception {
        SkipFilter f = new SkipFilter(new DisabledDownloadSymlinkResolver(), new NullSession(new Host(new TestProtocol())));
        assertTrue(f.accept(new Path("a", EnumSet.of(Path.Type.file)) {
                            }, new NullLocal("a", "b") {
                                @Override
                                public boolean exists() {
                                    return false;
                                }
                            }, new TransferStatus().exists(true)
                )
        );
        assertFalse(f.accept(new Path("a", EnumSet.of(Path.Type.file)) {
                             }, new NullLocal("a", "b") {
                                 @Override
                                 public boolean exists() {
                                     return true;
                                 }
                             }, new TransferStatus().exists(true)
                )
        );
    }

    @Test
    public void testAcceptDirectory() throws Exception {
        SkipFilter f = new SkipFilter(new DisabledDownloadSymlinkResolver(), new NullSession(new Host(new TestProtocol())));
        assertTrue(f.accept(new Path("a", EnumSet.of(Path.Type.directory)) {
                            }, new NullLocal("a", "b") {
                                @Override
                                public boolean isFile() {
                                    return false;
                                }

                                @Override
                                public boolean isDirectory() {
                                    return true;
                                }

                                @Override
                                public boolean exists() {
                                    return true;
                                }
                            }, new TransferStatus().exists(true)
                )
        );
    }
}
