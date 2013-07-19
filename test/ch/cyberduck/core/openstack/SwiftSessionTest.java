package ch.cyberduck.core.openstack;

import ch.cyberduck.core.*;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Lifecycle;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.local.LocalFactory;
import ch.cyberduck.core.serializer.impl.ProfileReaderFactory;

import org.junit.Test;

import java.util.UUID;

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
        assertNotNull(session.mount(new DisabledListProgressListener()));
        assertTrue(session.isConnected());
        assertFalse(session.cache().isEmpty());
        final Path container = new Path("/test.cyberduck.ch", Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        assertNull(session.toHttpURL(new Path(container, "d/f", Path.FILE_TYPE)));
        assertNotNull(session.getFeature(DistributionConfiguration.class, null));
        container.attributes().setRegion("DFW");
        assertEquals("http://2b72124779a6075376a9-dc3ef5db7541ebd1f458742f9170bbe4.r64.cf1.rackcdn.com/d/f", session.toHttpURL(new Path(container, "d/f", Path.FILE_TYPE)));
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
        assertNotNull(session.mount(new DisabledListProgressListener()));
        assertFalse(session.cache().isEmpty());
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
        assertNotNull(session.mount(new DisabledListProgressListener()));
        assertFalse(session.cache().isEmpty());
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
    }

    @Test
    public void testFile() {
        final SwiftSession session = new SwiftSession(new Host(Protocol.SWIFT, "h"));
        assertFalse(session.isCreateFileSupported(new Path("/", Path.VOLUME_TYPE)));
        assertTrue(session.isCreateFileSupported(new Path("/container", Path.VOLUME_TYPE)));
    }

    @Test
    public void testCreateContainer() throws Exception {
        final Host host = new Host(Protocol.SWIFT, "identity.api.rackspacecloud.com", new Credentials(
                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path(UUID.randomUUID().toString(), Path.DIRECTORY_TYPE);
        session.mkdir(container, null);
        assertTrue(session.exists(container));
        session.delete(container, new DisabledLoginController());
        assertFalse(session.exists(container));
    }

    @Test
    public void testCreatePlaceholder() throws Exception {
        final Host host = new Host(Protocol.SWIFT, "identity.api.rackspacecloud.com", new Credentials(
                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("/test.cyberduck.ch", Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        final Path placeholder = new Path(container, UUID.randomUUID().toString(), Path.DIRECTORY_TYPE);
        session.mkdir(placeholder, null);
        assertTrue(session.exists(placeholder));
        session.delete(placeholder, new DisabledLoginController());
        assertFalse(session.exists(placeholder));
    }
}
