package ch.cyberduck.core.openstack;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Lifecycle;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.shared.DefaultVersioningFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;

import static org.junit.Assert.*;

public class SwiftSessionTest extends AbstractSwiftTest {

    @Test
    public void testFeatures() {
        assertEquals(DefaultVersioningFeature.class, session.getFeature(Versioning.class).getClass());
        assertNull(session.getFeature(Lifecycle.class));
        assertNotNull(session.getFeature(Copy.class));
        assertNotNull(session.getFeature(Location.class));
        assertNull(session.getFeature(Encryption.class));
        assertNull(session.getFeature(Redundancy.class));
        assertNull(session.getFeature(Logging.class));
        assertNotNull(session.getFeature(DistributionConfiguration.class));
    }

    @Test
    public void testConnect() throws Exception {
        assertTrue(session.isConnected());
        final Path container = new Path("/test.cyberduck.ch", EnumSet.of(Path.Type.volume, Path.Type.directory));
        assertEquals(DescriptiveUrl.EMPTY, session.getFeature(UrlProvider.class).toUrl(new Path(container, "d/f", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.cdn));
        final DistributionConfiguration cdn = session.getFeature(DistributionConfiguration.class);
        assertNotNull(cdn);
        session.close();
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailure() throws Exception {
        final Host host = bookmark(new Credentials("a", "s"));
        final SwiftSession session = new SwiftSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(new DisabledProxyFinder(), HostKeyCallback.noop, LoginCallback.noop, CancelCallback.noop));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(LoginCallback.noop, CancelCallback.noop);
    }
}
