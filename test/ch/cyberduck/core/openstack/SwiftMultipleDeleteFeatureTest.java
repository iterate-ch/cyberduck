package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class SwiftMultipleDeleteFeatureTest extends AbstractTestCase {

    @Test
    public void testDelete() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path dfw = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        dfw.attributes().setRegion("DFW");
        final Path test = new Path(dfw, UUID.randomUUID().toString(), Path.FILE_TYPE);
        new SwiftTouchFeature(session).touch(test);
        assertTrue(new SwiftFindFeature(session).find(test));
        new SwiftMultipleDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginController());
        assertFalse(new SwiftFindFeature(session).find(test));
        session.close();
    }

    @Test(expected = NotfoundException.class)
    @Ignore
    public void testDeleteNotFoundKey() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        new SwiftMultipleDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginController());
    }
}
