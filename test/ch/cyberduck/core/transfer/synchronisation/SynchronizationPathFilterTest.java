package ch.cyberduck.core.transfer.synchronisation;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.synchronization.ComparisionServiceFilter;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.download.OverwriteFilter;
import ch.cyberduck.core.transfer.symlink.DownloadSymlinkResolver;
import ch.cyberduck.core.transfer.symlink.UploadSymlinkResolver;

import org.junit.Test;

import java.util.Collections;
import java.util.TimeZone;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SynchronizationPathFilterTest extends AbstractTestCase {

    @Test
    public void testPrepare() throws Exception {
        final Path test = new Path("/t/a", Path.FILE_TYPE);
        test.setLocal(new NullLocal(System.getProperty("java.io.tmpdir"), "t") {
            @Override
            public LocalAttributes attributes() {
                return new LocalAttributes(this.getAbsolute()) {
                    @Override
                    public long getSize() {
                        return 1L;
                    }
                };
            }
        });
        Session session = new NullSession(new Host("t")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> list = new AttributedList<Path>();
                if(file.equals(test.getParent())) {
                    final Path f = new Path("/t/a", Path.FILE_TYPE);
                    f.attributes().setSize(3L);
                    list.add(f);
                }
                return list;
            }
        };
        final SynchronizationPathFilter mirror = new SynchronizationPathFilter(new ComparisionServiceFilter(session, TimeZone.getDefault()),
                new OverwriteFilter(new DownloadSymlinkResolver(Collections.<Path>emptyList()), session),
                new ch.cyberduck.core.transfer.upload.OverwriteFilter(new UploadSymlinkResolver(null, Collections.<Path>emptyList()), session),
                TransferAction.mirror);
        assertTrue(mirror.accept(test, new TransferStatus().exists(true)));
        final TransferStatus status = mirror.prepare(test, new TransferStatus().exists(true));
        assertNotNull(status);
        assertEquals(1L, status.getLength());
        final SynchronizationPathFilter download = new SynchronizationPathFilter(new ComparisionServiceFilter(session, TimeZone.getDefault()),
                new OverwriteFilter(new DownloadSymlinkResolver(Collections.<Path>emptyList()), session),
                new ch.cyberduck.core.transfer.upload.OverwriteFilter(new UploadSymlinkResolver(null, Collections.<Path>emptyList()), session),
                TransferAction.download);
        assertFalse(download.accept(test, new TransferStatus().exists(true)));
        final SynchronizationPathFilter upload = new SynchronizationPathFilter(new ComparisionServiceFilter(session, TimeZone.getDefault()),
                new OverwriteFilter(new DownloadSymlinkResolver(Collections.<Path>emptyList()), session),
                new ch.cyberduck.core.transfer.upload.OverwriteFilter(new UploadSymlinkResolver(null, Collections.<Path>emptyList()), session),
                TransferAction.upload);
        assertTrue(upload.accept(test, new TransferStatus().exists(true)));
    }
}
