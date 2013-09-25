package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class SwiftContainerListServiceTest extends AbstractTestCase {

    @Test
    public void testList() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final List<Path> list = new SwiftContainerListService(false, false).list(session);
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        container.attributes().setRegion("DFW");
        assertTrue(list.contains(container));
        container.attributes().setRegion("ORD");
        assertFalse(list.contains(container));
    }
}
