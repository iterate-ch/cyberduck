package ch.cyberduck.core.dav;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.ui.action.DeleteWorker;

import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class DAVDeleteFeatureTest extends AbstractTestCase {

    @Test
    public void testDeleteDirectory() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                properties.getProperty("webdav.user"), properties.getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path(session.home(), UUID.randomUUID().toString(), Path.DIRECTORY_TYPE);
        session.mkdir(test, null);
        assertTrue(session.exists(test));
        session.getFeature(Touch.class, new DisabledLoginController()).touch(new Path(test, UUID.randomUUID().toString(), Path.FILE_TYPE));
        new DeleteWorker(session, new DisabledLoginController(), Collections.singletonList(test)) {
            @Override
            public void cleanup(final Void result) {
                //
            }
        }.run();
        assertFalse(session.exists(test));
        session.close();
    }
}
