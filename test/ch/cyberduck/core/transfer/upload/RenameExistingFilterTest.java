package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.NullSymlinkResolver;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class RenameExistingFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        final RenameExistingFilter f = new RenameExistingFilter(new NullSymlinkResolver());
        final Path t = new Path("t", Path.FILE_TYPE);
        t.setLocal(new NullLocal("/Downloads", "n"));
        assertTrue(f.accept(new NullSession(new Host("h")), t, new TransferStatus()));
    }

    @Test
    public void testPrepare() throws Exception {
        final RenameExistingFilter f = new RenameExistingFilter(new NullSymlinkResolver());
        final Path p = new Path("t", Path.FILE_TYPE) {

            @Override
            public Path getParent() {
                return new Path("p", Path.DIRECTORY_TYPE);
            }
        };
        p.setLocal(new NullLocal("/Downloads", "n"));
        final AtomicBoolean c = new AtomicBoolean();
        f.prepare(new NullSession(new Host("h")) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                if(type == Move.class) {
                    return (T) new Move() {
                        @Override
                        public void move(final Path file, final Path renamed) throws BackgroundException {
                            assertNotSame(file.getName(), renamed.getName());
                            c.set(true);
                        }

                        @Override
                        public boolean isSupported(final Path file) {
                            return true;
                        }
                    };
                }
                return null;
            }

            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> l = new AttributedList<Path>();
                l.add(new Path("t", Path.FILE_TYPE));
                return l;
            }
        }, p, new ch.cyberduck.core.transfer.TransferStatus());
        assertTrue(c.get());
    }
}
