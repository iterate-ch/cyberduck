package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultDownloadFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.DisabledDownloadSymlinkResolver;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

public class ResumeFilterTest {

    @Test
    public void testAcceptDirectory() throws Exception {
        ResumeFilter f = new ResumeFilter(new DisabledDownloadSymlinkResolver(), new NullSession(new Host(new TestProtocol())));
        Path p = new Path("a", EnumSet.of(Path.Type.directory));
        assertTrue(f.accept(p, new NullLocal("d", "a") {
            @Override
            public boolean isDirectory() {
                return true;
            }

            @Override
            public boolean isFile() {
                return false;
            }
        }, new TransferStatus()));
    }

    @Test
    public void testAcceptExistsFalse() throws Exception {
        ResumeFilter f = new ResumeFilter(new DisabledDownloadSymlinkResolver(), new NullSession(new Host(new TestProtocol())));
        Path p = new Path("a", EnumSet.of(Path.Type.file));
        p.attributes().setSize(2L);
        assertTrue(f.accept(p, new NullLocal("~/Downloads", "a") {
            @Override
            public boolean exists() {
                return false;
            }
        }, new TransferStatus()));
    }

    @Test
    public void testPrepareFile() throws Exception {
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        ResumeFilter f = new ResumeFilter(new DisabledDownloadSymlinkResolver(), session,
                new DownloadFilterOptions(), new DefaultDownloadFeature(session) {
            @Override
            public boolean offset(final Path file) throws BackgroundException {
                return true;
            }
        });
        Path p = new Path("a", EnumSet.of(Path.Type.file));
        final NullLocal local = new NullLocal("~/Downloads", "a") {
            @Override
            public LocalAttributes attributes() {
                return new LocalAttributes("a") {
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
        p.attributes().setSize(2L);
        final TransferStatus status = f.prepare(p, local, new TransferStatus());
        assertTrue(status.isAppend());
        assertEquals(1L, status.getOffset(), 0L);
    }

    @Test
    public void testPrepareDirectoryExists() throws Exception {
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        ResumeFilter f = new ResumeFilter(new DisabledDownloadSymlinkResolver(), session,
                new DownloadFilterOptions(), new DefaultDownloadFeature(session) {
            @Override
            public boolean offset(final Path file) throws BackgroundException {
                return true;
            }
        });
        Path p = new Path("a", EnumSet.of(Path.Type.directory));
        final NullLocal local = new NullLocal("a") {
            @Override
            public boolean exists() {
                return true;
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
        final TransferStatus status = f.prepare(p, local, new TransferStatus().exists(true));
        assertTrue(status.isExists());
    }

    @Test
    public void testPrepareDirectoryExistsFalse() throws Exception {
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        ResumeFilter f = new ResumeFilter(new DisabledDownloadSymlinkResolver(), session,
                new DownloadFilterOptions(), new DefaultDownloadFeature(session) {
            @Override
            public boolean offset(final Path file) throws BackgroundException {
                return true;
            }
        });
        Path p = new Path("a", EnumSet.of(Path.Type.directory));
        final NullLocal local = new NullLocal("a") {
            @Override
            public boolean exists() {
                return false;
            }
        };
        final TransferStatus status = f.prepare(p, local, new TransferStatus());
        assertFalse(status.isAppend());
    }
}
