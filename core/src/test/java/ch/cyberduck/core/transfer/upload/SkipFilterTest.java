package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.DisabledUploadSymlinkResolver;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SkipFilterTest {

    @Test
    public void testAccept() throws Exception {
        SkipFilter f = new SkipFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getFeature(Class<T> type) {
                if(type == Find.class) {
                    return (T) new Find() {
                        @Override
                        public boolean find(Path file) throws BackgroundException {
                            return true;
                        }

                        @Override
                        public Find withCache(PathCache cache) {
                            return this;
                        }
                    };
                }
                return super.getFeature(type);
            }
        });
        assertFalse(f.accept(new Path("a", EnumSet.of(Path.Type.file)), new NullLocal("a") {
            @Override
            public boolean exists() {
                return true;
            }
        }, new TransferStatus().exists(true)));
    }

    @Test
    public void testAcceptDirectory() throws Exception {
        SkipFilter f = new SkipFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return AttributedList.emptyList();
            }
        });
        f.withAttributes(new Attributes() {
            @Override
            public PathAttributes find(final Path file) throws BackgroundException {
                return file.attributes();
            }

            @Override
            public Attributes withCache(final PathCache cache) {
                return this;
            }
        });
        assertTrue(f.accept(new Path("a", EnumSet.of(Path.Type.directory)), new NullLocal("a") {
            @Override
            public boolean exists() {
                return true;
            }
        }, new TransferStatus().exists(true)));
    }

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        SkipFilter f = new SkipFilter(new DisabledUploadSymlinkResolver(), new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return AttributedList.emptyList();
            }
        });
        f.withAttributes(new Attributes() {
            @Override
            public PathAttributes find(final Path file) throws BackgroundException {
                return file.attributes();
            }

            @Override
            public Attributes withCache(final PathCache cache) {
                return this;
            }
        });
        assertFalse(f.accept(new Path("a", EnumSet.of(Path.Type.file)), new NullLocal("a") {
            @Override
            public boolean exists() {
                return false;
            }
        }, new TransferStatus().exists(true)));
    }
}
