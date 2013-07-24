package ch.cyberduck.ui.cocoa.threading;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.HostKeyController;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.ui.Controller;
import ch.cyberduck.ui.HostKeyControllerFactory;
import ch.cyberduck.ui.LoginControllerFactory;
import ch.cyberduck.ui.cocoa.BrowserController;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class BrowserBackgroundActionTest extends AbstractTestCase {

    @Test
    public void testGetSessions() throws Exception {
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
        assertTrue(new BrowserBackgroundAction(new BrowserController() {
            @Override
            protected String getBundleName() {
                return null;
            }
        }) {
            @Override
            public Boolean run() throws BackgroundException {
                return false;
            }
        }.getSessions().isEmpty());
    }
}