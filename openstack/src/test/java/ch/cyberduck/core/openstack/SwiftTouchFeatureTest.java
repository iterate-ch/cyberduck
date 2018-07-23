package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftTouchFeatureTest {

    @Test
    public void testSupported() {
        final SwiftSession session = new SwiftSession(new Host(new SwiftProtocol(), "h"));
        assertFalse(new SwiftTouchFeature(session, new SwiftRegionService(session)).isSupported(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertTrue(new SwiftTouchFeature(session, new SwiftRegionService(session)).isSupported(new Path("/container", EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testTouch() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
            System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final Path test = new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(
            new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(test.attributes().getChecksum(), new SwiftAttributesFinderFeature(session).find(test).getChecksum());
        session.close();
    }
}
