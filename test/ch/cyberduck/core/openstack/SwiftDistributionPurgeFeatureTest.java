package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cdn.Distribution;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

/**
 * @version $Id:$
 */
public class SwiftDistributionPurgeFeatureTest extends AbstractTestCase {

    @Test
    @Ignore
    public void testInvalidateContainer() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final SwiftDistributionPurgeFeature feature = new SwiftDistributionPurgeFeature(session);
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        feature.invalidate(container, Distribution.DOWNLOAD, Collections.singletonList(container), new DisabledLoginController());
        session.close();
    }

    @Test
    public void testInvalidateFile() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final SwiftDistributionPurgeFeature feature = new SwiftDistributionPurgeFeature(session);
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        feature.invalidate(container, Distribution.DOWNLOAD, Collections.singletonList(new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE)), new DisabledLoginController());
        session.close();
    }
}
