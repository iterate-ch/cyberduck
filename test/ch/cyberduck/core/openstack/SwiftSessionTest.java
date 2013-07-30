package ch.cyberduck.core.openstack;

import ch.cyberduck.core.*;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Lifecycle;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.features.Versioning;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SwiftSessionTest extends AbstractTestCase {

    @Test
    public void testFeatures() throws Exception {
        final SwiftSession session = new SwiftSession(new Host(Protocol.SWIFT, "identity.api.rackspacecloud.com"));
        assertNull(session.getFeature(Versioning.class, null));
        assertNotNull(session.getFeature(AnalyticsProvider.class, null));
        assertNull(session.getFeature(Lifecycle.class, null));
        assertNotNull(session.getFeature(Copy.class, null));
        assertNotNull(session.getFeature(Location.class, null));
        assertNull(session.getFeature(Encryption.class, null));
        assertNull(session.getFeature(Redundancy.class, null));
        assertNull(session.getFeature(Logging.class, null));
        assertNull(session.getFeature(DistributionConfiguration.class, null));
    }

    @Test
    public void testConnectRackspace() throws Exception {
        final Host host = new Host(Protocol.SWIFT, "identity.api.rackspacecloud.com", new Credentials(
                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.workdir());
        assertTrue(session.isConnected());
        final Path container = new Path("/test.cyberduck.ch", Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        container.attributes().setRegion("DFW");
        assertEquals(DescriptiveUrl.EMPTY, session.getURLs(new Path(container, "d/f", Path.FILE_TYPE)).find(DescriptiveUrl.Type.cdn));
        final DistributionConfiguration cdn = session.getFeature(DistributionConfiguration.class, null);
        assertNotNull(cdn);
        cdn.read(container, Distribution.DOWNLOAD);
        assertEquals("http://2b72124779a6075376a9-dc3ef5db7541ebd1f458742f9170bbe4.r64.cf1.rackcdn.com/d/f",
                session.getURLs(new Path(container, "d/f", Path.FILE_TYPE)).find(DescriptiveUrl.Type.cdn).getUrl());
        session.close();
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
    }

    @Test
    public void testConnectRackspaceLon() throws Exception {
        final Profile profile = ProfileReaderFactory.get().read(
                LocalFactory.createLocal("profiles/Rackspace UK.cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.workdir());
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailure() throws Exception {
        final Host host = new Host(Protocol.SWIFT, "identity.api.rackspacecloud.com", new Credentials(
                "a", "s"
        ));
        final SwiftSession session = new SwiftSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        try {
            session.login(new DisabledPasswordStore(), new DisabledLoginController());
            fail();
        }
        catch(LoginFailureException e) {
            throw e;
        }
    }

    @Test
    public void testConnectHp() throws Exception {
        final Host host = new Host(Protocol.SWIFT, "region-a.geo-1.identity.hpcloudsvc.com", 35357, new Credentials(
                properties.getProperty("hpcloud.key"), properties.getProperty("hpcloud.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.workdir());
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
    }
}
