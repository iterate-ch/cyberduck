package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
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
        f.prepare(t, new NullLocal("t"), new TransferStatus());
        assertNotSame("t", t.getName());
    }

    @Test
    public void testDirectoryUpload() throws Exception {
        final Path file = new Path("/t", EnumSet.of(Path.Type.directory));
        final AtomicBoolean found = new AtomicBoolean();
        final AtomicBoolean moved = new AtomicBoolean();
        final AttributesFinder attributes = new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file) throws BackgroundException {
                return new PathAttributes();
            }

            @Override
            public AttributesFinder withCache(PathCache cache) {
                return this;
            }
        };
        final Find find = new Find() {
            @Override
            public boolean find(final Path f) throws BackgroundException {
                if(f.equals(file)) {
                    found.set(true);
                    return true;
                }
                return false;
            }

            @Override
            public Find withCache(PathCache cache) {
                return this;
            }
        };
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
        };
        final RenameFilter f = new RenameFilter(new DisabledUploadSymlinkResolver(), session);
        f.withFinder(find).withAttributes(attributes);
        final TransferStatus status = f.prepare(file, new NullLocal("t"), new TransferStatus().exists(true));
        assertTrue(found.get());
        assertNotNull(status.getRename());
        assertNull(status.getRename().local);
        assertNotNull(status.getRename().remote);
    }
}