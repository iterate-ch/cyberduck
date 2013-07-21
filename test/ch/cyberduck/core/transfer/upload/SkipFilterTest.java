package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.NullSymlinkResolver;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class SkipFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        SkipFilter f = new SkipFilter(new NullSymlinkResolver());
        assertTrue(f.accept(new NullSession(new Host("h")), new Path("a", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "a") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                };
            }
        }, new TransferStatus()));
        assertFalse(f.accept(new NullSession(new Host("h")) {
                                 @Override
                                 public boolean exists(final Path path) throws BackgroundException {
                                     return true;
                                 }
                             }, new Path("a", Path.FILE_TYPE) {
                                 @Override
                                 public Local getLocal() {
                                     return new NullLocal(null, "a") {
                                         @Override
                                         public boolean exists() {
                                             return false;
                                         }
                                     };
                                 }
                             }, new TransferStatus()
        ));
    }
}
