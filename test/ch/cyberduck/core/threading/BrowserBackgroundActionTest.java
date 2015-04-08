package ch.cyberduck.core.threading;

import ch.cyberduck.core.AbstractController;
import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.test.NullSession;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @version $Id$
 */
public class BrowserBackgroundActionTest extends AbstractTestCase {

    @Test
    public void testGetSessions() throws Exception {
        assertNotNull(new BrowserBackgroundAction(new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                throw new UnsupportedOperationException();
            }
        }, new NullSession(new Host("t")), PathCache.empty()) {
            @Override
            public Boolean run() throws BackgroundException {
                return false;
            }
        }.session);
    }
}