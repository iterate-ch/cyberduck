package ch.cyberduck.core.threading;

import ch.cyberduck.core.AbstractController;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.pool.SingleSessionPool;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class RegistryBackgroundActionTest {

    @Test
    public void testGetSessions() throws Exception {
        assertNotNull(new RegistryBackgroundAction<Boolean>(new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                throw new UnsupportedOperationException();
            }
        }, new SingleSessionPool(new NullSession(new Host(new TestProtocol())))) {
            @Override
            public Boolean run(final Session<?> session) throws BackgroundException {
                return false;
            }
        }.pool);
    }
}