package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class SwiftMultipleDeleteFeatureTest extends AbstractTestCase {

    @Test
    public void testDeleteHP() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume));
        container.attributes().setRegion("region-a.geo-1");
        this.delete(new Host(new SwiftProtocol() {
            @Override
            public String getContext() {
                return "/v2.0/tokens";
            }
        }, "region-a.geo-1.identity.hpcloudsvc.com", 35357, new Credentials(
                properties.getProperty("hpcloud.key"), properties.getProperty("hpcloud.secret")
        )), container);
    }

    @Test
    public void testDeleteRAX() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume));
        container.attributes().setRegion("DFW");
        this.delete(new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                new Credentials(
                        properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret"))), container);
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
        new SwiftMultipleDeleteFeature(session).delete(Arrays.asList(test1, test2), new DisabledLoginCallback(), new DisabledProgressListener());
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
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume));
        new SwiftMultipleDeleteFeature(session).delete(Arrays.asList(
                new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)),
                new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))
        ), new DisabledLoginCallback(), new DisabledProgressListener());
    }
}
