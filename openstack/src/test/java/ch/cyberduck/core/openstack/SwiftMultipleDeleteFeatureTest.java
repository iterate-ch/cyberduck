package ch.cyberduck.core.openstack;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
@Category(IntegrationTest.class)
public class SwiftMultipleDeleteFeatureTest {

    @Test
    public void testDeleteRAX() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume));
        container.attributes().setRegion("DFW");
        this.delete(new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                new Credentials(
                        System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret"))), container);
    }

    protected void delete(final Host host, final Path container) throws Exception {
        final SwiftSession session = new SwiftSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path test1 = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Path test2 = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new SwiftTouchFeature(session).touch(test1);
        new SwiftTouchFeature(session).touch(test2);
        assertTrue(new SwiftFindFeature(session).find(test1));
        assertTrue(new SwiftFindFeature(session).find(test2));
        new SwiftMultipleDeleteFeature(session).delete(Arrays.asList(test1, test2), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        Thread.sleep(1000L);
        assertFalse(new SwiftFindFeature(session).find(test1));
        assertFalse(new SwiftFindFeature(session).find(test2));
        session.close();
    }

    @Test(expected = NotfoundException.class)
    @Ignore
    public void testDeleteNotFoundKey() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume));
        new SwiftMultipleDeleteFeature(session).delete(Arrays.asList(
                new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)),
                new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))
        ), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
    }
}
