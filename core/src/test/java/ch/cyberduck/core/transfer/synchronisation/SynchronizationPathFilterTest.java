package ch.cyberduck.core.transfer.synchronisation;

import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.synchronization.ComparePathFilter;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonServiceFilter;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.download.OverwriteFilter;
import ch.cyberduck.core.transfer.symlink.DownloadSymlinkResolver;
import ch.cyberduck.core.transfer.symlink.UploadSymlinkResolver;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class SynchronizationPathFilterTest {

    @Test
    public void testPrepare() throws Exception {
        final Path test = new Path("/t/a", EnumSet.of(Path.Type.file));
        Session session = new NullSession(new Host(new TestProtocol()));
        final NullLocal local = new NullLocal(System.getProperty("java.io.tmpdir"), "t") {
            @Override
            public boolean isSymbolicLink() {
                return false;
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

            @Override
            public boolean isFile() {
                return true;
            }

            @Override
            public boolean exists() {
                return true;
            }
        };
        final SynchronizationPathFilter mirror = new SynchronizationPathFilter(
                new ComparisonServiceFilter(session, TimeZone.getDefault(), new DisabledProgressListener()),
                new OverwriteFilter(new DownloadSymlinkResolver(Collections.<TransferItem>emptyList()), session),
                new ch.cyberduck.core.transfer.upload.OverwriteFilter(new UploadSymlinkResolver(null, Collections.<TransferItem>emptyList()), session),
                TransferAction.mirror);
        assertTrue(mirror.accept(test, local, new TransferStatus().exists(true)));
        final TransferStatus status = mirror.prepare(test, local, new TransferStatus().exists(true));
        assertNotNull(status);
        assertEquals(1L, status.getLength());
        final SynchronizationPathFilter download = new SynchronizationPathFilter(
                new ComparisonServiceFilter(session, TimeZone.getDefault(), new DisabledProgressListener()),
                new OverwriteFilter(new DownloadSymlinkResolver(Collections.<TransferItem>emptyList()), session),
                new ch.cyberduck.core.transfer.upload.OverwriteFilter(new UploadSymlinkResolver(null, Collections.<TransferItem>emptyList()), session),
                TransferAction.download);
        assertFalse(download.accept(test, local, new TransferStatus().exists(true)));
        final SynchronizationPathFilter upload = new SynchronizationPathFilter(
                new ComparisonServiceFilter(session, TimeZone.getDefault(), new DisabledProgressListener()),
                new OverwriteFilter(new DownloadSymlinkResolver(Collections.<TransferItem>emptyList()), session),
                new ch.cyberduck.core.transfer.upload.OverwriteFilter(new UploadSymlinkResolver(null, Collections.<TransferItem>emptyList()), session),
                TransferAction.upload);
        assertTrue(upload.accept(test, local, new TransferStatus().exists(true)));
    }

    @Test
    public void testAcceptDirectory() throws Exception {
        Session session = new NullSession(new Host(new TestProtocol()));
        final SynchronizationPathFilter mirror = new SynchronizationPathFilter(new ComparePathFilter() {
            @Override
            public Comparison compare(Path file, Local local) throws BackgroundException {
                return Comparison.equal;
            }
        }, new OverwriteFilter(new DownloadSymlinkResolver(Collections.<TransferItem>emptyList()), session),
                new ch.cyberduck.core.transfer.upload.OverwriteFilter(new UploadSymlinkResolver(null, Collections.<TransferItem>emptyList()), session),
                TransferAction.mirror
        );
        assertTrue(mirror.accept(new Path("/p", EnumSet.of(Path.Type.directory)), null, new TransferStatus().exists(true)));
    }
}
