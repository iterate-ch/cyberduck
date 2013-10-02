package ch.cyberduck.ui.threading;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyController;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.MainAction;
import ch.cyberduck.ui.AbstractController;
import ch.cyberduck.ui.Controller;
import ch.cyberduck.ui.HostKeyControllerFactory;
import ch.cyberduck.ui.LoginControllerFactory;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * @version $Id$
 */
public class BrowserBackgroundActionTest extends AbstractTestCase {

    @Before
    public void factory() {
        LoginControllerFactory.addFactory(Factory.NATIVE_PLATFORM, new LoginControllerFactory() {
            @Override
            protected LoginController create(final Controller c) {
                return null;
            }

            @Override
            protected LoginController create() {
                return null;
            }
        });
        HostKeyControllerFactory.addFactory(Factory.NATIVE_PLATFORM, new HostKeyControllerFactory() {

            @Override
            public HostKeyController create(final Controller c) {
                return null;
            }

            @Override
            protected HostKeyController create() {
                return null;
            }
        });
    }

    @Test
    public void testGetSessions() throws Exception {
        assertFalse(new BrowserBackgroundAction(new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                throw new UnsupportedOperationException();
            }
        }, new NullSession(new Host("t")), Cache.empty()) {
            @Override
            public Boolean run() throws BackgroundException {
                return false;
            }
        }.getSessions().isEmpty());
    }
}