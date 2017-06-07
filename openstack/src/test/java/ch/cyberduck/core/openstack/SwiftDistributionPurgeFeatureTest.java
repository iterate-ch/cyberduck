package ch.cyberduck.core.openstack;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

@Ignore
@Category(IntegrationTest.class)
public class SwiftDistributionPurgeFeatureTest {

    @Test(expected = InteroperabilityException.class)
    public void testInvalidateContainer() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final SwiftDistributionPurgeFeature feature = new SwiftDistributionPurgeFeature(session);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume, Path.Type.directory));
        container.attributes().setRegion("DFW");
        feature.invalidate(container, Distribution.DOWNLOAD, Collections.singletonList(container), new DisabledLoginCallback());
        session.close();
    }

    @Test
    public void testInvalidateFile() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final SwiftDistributionPurgeFeature feature = new SwiftDistributionPurgeFeature(session);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume, Path.Type.directory));
        container.attributes().setRegion("DFW");
        feature.invalidate(container, Distribution.DOWNLOAD, Collections.singletonList(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))), new DisabledLoginCallback());
        session.close();
    }
}
