package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.exception.InteroperabilityException;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

/**
 * @version $Id$
 */
public class SwiftDistributionPurgeFeatureTest extends AbstractTestCase {

    @Test(expected = InteroperabilityException.class)
    public void testInvalidateContainer() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final SwiftDistributionPurgeFeature feature = new SwiftDistributionPurgeFeature(session);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume, Path.Type.directory));
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
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final SwiftDistributionPurgeFeature feature = new SwiftDistributionPurgeFeature(session);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume, Path.Type.directory));
        feature.invalidate(container, Distribution.DOWNLOAD, Collections.singletonList(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))), new DisabledLoginController());
        session.close();
    }
}
