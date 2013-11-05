package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.shared.DefaultAttributesFeature;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SwiftFindFeatureTest extends AbstractTestCase {

    @Test
    public void testFindContainer() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        container.attributes().setRegion("DFW");
        assertTrue(new SwiftFindFeature(session).find(container));
        session.close();
    }

    @Test
    public void testFindKey() throws Exception {
        // Test with and without region
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        container.attributes().setRegion("DFW");
        final Path file = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        assertFalse(new SwiftFindFeature(session).find(file));
        try {
            new DefaultAttributesFeature(session).find(file);
            fail();
        }
        catch(NotfoundException e) {
            //
        }
        new SwiftTouchFeature(session).touch(file);
        assertTrue(new SwiftFindFeature(session).find(file));
        assertNotNull(new DefaultAttributesFeature(session).find(file));
        session.close();
    }
}
