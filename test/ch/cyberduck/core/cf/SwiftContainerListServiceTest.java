package ch.cyberduck.core.cf;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;

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
        final CFSession session = new CFSession(
                new Host(Protocol.CLOUDFILES, Protocol.CLOUDFILES.getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open();
        session.login(new DisabledLoginController());
        final List<Path> list = new SwiftContainerListService().list(session);
        final CFPath container = new CFPath(session, "test.cyberduck.ch", Path.VOLUME_TYPE);
        container.attributes().setRegion("DFW");
        assertTrue(list.contains(container));
        container.attributes().setRegion("ORD");
        assertFalse(list.contains(container));
    }
}
