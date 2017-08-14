package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SwiftContainerListServiceTest {

    @Test
    public void testList() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final AttributedList<Path> list = new SwiftContainerListService(session, new SwiftLocationFeature.SwiftRegion(null)
        ).list(new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)), new DisabledListProgressListener());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume, Path.Type.directory));
        container.attributes().setRegion("DFW");
        assertTrue(list.contains(container));
        container.attributes().setRegion("ORD");
        assertTrue(list.contains(container));
        container.attributes().setRegion("ORD1");
        assertFalse(list.contains(container));
        session.close();
    }

    @Test
    public void testListLimitRegion() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final AttributedList<Path> list = new SwiftContainerListService(session, new SwiftLocationFeature.SwiftRegion("ORD")
        ).list(new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)), new DisabledListProgressListener());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume, Path.Type.directory));
        container.attributes().setRegion("DFW");
        assertFalse(list.contains(container));
        container.attributes().setRegion("ORD");
        assertTrue(list.contains(container));
        session.close();
    }
}
