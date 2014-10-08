package ch.cyberduck.core.openstack;

import ch.cyberduck.core.*;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Lifecycle;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.features.Versioning;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SwiftSessionTest extends AbstractTestCase {

    @Test
    public void testFeatures() throws Exception {
        final SwiftSession session = new SwiftSession(new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com"));
        assertNull(session.getFeature(Versioning.class));
        assertNotNull(session.getFeature(AnalyticsProvider.class));
        assertNull(session.getFeature(Lifecycle.class));
        assertNotNull(session.getFeature(Copy.class));
        assertNotNull(session.getFeature(Location.class));
        assertNull(session.getFeature(Encryption.class));
        assertNull(session.getFeature(Redundancy.class));
        assertNull(session.getFeature(Logging.class));
        assertNotNull(session.getFeature(DistributionConfiguration.class));
    }

    @Test
    public void testConnectRackspace() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        new LoginConnectionService(new DisabledLoginController(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, Cache.<Path>empty());
        assertNotNull(session.workdir());
        assertTrue(session.isConnected());
        final Path container = new Path("/test.cyberduck.ch", EnumSet.of(Path.Type.volume, Path.Type.directory));
        container.attributes().setRegion("DFW");
        assertEquals(DescriptiveUrl.EMPTY, session.getFeature(UrlProvider.class).toUrl(new Path(container, "d/f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.cdn));
        final DistributionConfiguration cdn = session.getFeature(DistributionConfiguration.class);
        assertNotNull(cdn);
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
        new LoginConnectionService(new DisabledLoginController(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, Cache.<Path>empty());
        assertNotNull(session.workdir());
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailure() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                "a", "s"
        ));
        final SwiftSession session = new SwiftSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback());
    }

    @Test
    public void testConnectHpv1() throws Exception {
        final Host host = new Host(new SwiftProtocol() {
            @Override
            public String getContext() {
                return "/v1.0";
            }
        }, "region-a.geo-1.identity.hpcloudsvc.com", 35357, new Credentials(
                properties.getProperty("hpcloud.user"), properties.getProperty("hpcloud.password")
        ));
        final SwiftSession session = new SwiftSession(host);
        new LoginConnectionService(new DisabledLoginController(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, Cache.<Path>empty());
        assertNotNull(session.workdir());
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
    }

    @Test
    public void testConnectHpv2() throws Exception {
        final SwiftProtocol protocol = new SwiftProtocol() {
            @Override
            public String getContext() {
                return "/v2.0/tokens";
            }
        };
        final Host host = new Host(protocol, "region-a.geo-1.identity.hpcloudsvc.com", 35357, new Credentials(
                properties.getProperty("hpcloud.key"), properties.getProperty("hpcloud.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        new LoginConnectionService(new DisabledLoginController(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, Cache.<Path>empty());
        assertNotNull(session.workdir());
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
    }

    @Test(expected = LoginFailureException.class)
    public void testConnectOraclecloud() throws Exception {
        final SwiftProtocol protocol = new SwiftProtocol() {
            @Override
            public String getContext() {
                return "/auth/v1.0";
            }
        };
        final Host host = new Host(protocol, "storage.us2.oraclecloud.com", new Credentials(
                properties.getProperty("oraclecloud.key"), properties.getProperty("oraclecloud.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback());
    }

    @Test
    public void testConnectEvault() throws Exception {
        final SwiftProtocol protocol = new SwiftProtocol();
        final Host host = new Host(protocol, "auth.lts2.evault.com", new Credentials(
                properties.getProperty("evault.openstack.key"), properties.getProperty("evault.openstack.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials, String title, String reason, LoginOptions options) throws LoginCanceledException {
                //
            }
        }, new DisabledCancelCallback());
        assertNotNull(session.workdir());
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
    }
}
