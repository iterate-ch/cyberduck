package ch.cyberduck.core.threading;

import ch.cyberduck.core.AbstractController;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BrowserBackgroundActionTest {

    @Test
    public void testGetSessions() throws Exception {
        assertNotNull(new BrowserBackgroundAction(new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                throw new UnsupportedOperationException();
            }
        }, new NullSession(new Host(new TestProtocol())), PathCache.empty()) {
            @Override
            public Boolean run() throws BackgroundException {
                return false;
            }
        }.session);
    }
}