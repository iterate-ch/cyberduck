package ch.cyberduck.ui.threading;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.MainAction;
import ch.cyberduck.ui.AbstractController;

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
        }, new NullSession(new Host("t")), Cache.<Path>empty()) {
            @Override
            public Boolean run() throws BackgroundException {
                return false;
            }
        }.getSession());
    }
}