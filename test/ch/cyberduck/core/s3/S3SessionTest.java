package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Lifecycle;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.identity.IdentityConfiguration;

import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class S3SessionTest extends AbstractTestCase {

    @Test
    public void testConnect() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(), new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.workdir());
        assertTrue(session.cache().containsKey(new Path("/", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE).getReference()));
        assertNotNull(session.cache().lookup(new Path("/test.cyberduck.ch", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE).getReference()));
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
        session.open(new DefaultHostKeyController());
        assertTrue(session.isConnected());
        assertNotNull(session.workdir());
        session.close();
        assertFalse(session.isConnected());
    }

    @Test
    public void testFeatures() throws Exception {
        final S3Session aws = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        assertNotNull(aws.getFeature(Copy.class, null));
        assertNotNull(aws.getFeature(AclPermission.class, null));
        assertNotNull(aws.getFeature(Versioning.class, null));
        assertNotNull(aws.getFeature(AnalyticsProvider.class, null));
        assertNotNull(aws.getFeature(Lifecycle.class, null));
        assertNotNull(aws.getFeature(Location.class, null));
        assertNotNull(aws.getFeature(Encryption.class, null));
        assertNotNull(aws.getFeature(Redundancy.class, null));
        assertNotNull(aws.getFeature(Logging.class, null));
        assertNotNull(aws.getFeature(DistributionConfiguration.class, null));
        assertNotNull(aws.getFeature(IdentityConfiguration.class, null));
        assertNotNull(aws.getFeature(IdentityConfiguration.class, null));
        assertEquals(S3MultipleDeleteFeature.class, aws.getFeature(Delete.class, null).getClass());
        final S3Session o = new S3Session(new Host(Protocol.S3_SSL, "o"));
        assertNotNull(o.getFeature(Copy.class, null));
        assertNotNull(o.getFeature(AclPermission.class, null));
        assertNull(o.getFeature(Versioning.class, null));
        assertNull(o.getFeature(AnalyticsProvider.class, null));
        assertNull(o.getFeature(Lifecycle.class, null));
        assertNull(o.getFeature(Location.class, null));
        assertNull(o.getFeature(Encryption.class, null));
        assertNull(o.getFeature(Redundancy.class, null));
        assertNull(o.getFeature(Logging.class, null));
        assertNotNull(o.getFeature(DistributionConfiguration.class, null));
        assertNull(o.getFeature(IdentityConfiguration.class, null));
        assertEquals(S3DefaultDeleteFeature.class, o.getFeature(Delete.class, null).getClass());
    }
}
