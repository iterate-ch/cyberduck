package ch.cyberduck.core.s3;

import ch.cyberduck.core.*;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.features.*;
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
        assertNotNull(session.mount(new DisabledListProgressListener()));
        assertFalse(session.cache().isEmpty());
        assertTrue(session.cache().containsKey(new Path("/", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE).getReference()));
        assertNotNull(session.cache().lookup(new Path("/test.cyberduck.ch", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE).getReference()));
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
        session.open(new DefaultHostKeyController());
        assertTrue(session.isConnected());
        assertNotNull(session.mount(new DisabledListProgressListener()));
        session.close();
        assertFalse(session.isConnected());
    }

    @Test
    public void testConnectCnameAnonymous() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, "dist.springframework.org", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final S3Session session = new S3Session(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount(new DisabledListProgressListener()));
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
    }

    @Test
    public void testConnectBuckenameAnonymous() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, "dist.springframework.org.s3.amazonaws.com", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final S3Session session = new S3Session(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount(new DisabledListProgressListener()));
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
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

    @Test
    public void testMakeDirectory() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(), new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path(session.home(), UUID.randomUUID().toString(), Path.DIRECTORY_TYPE);
        session.mkdir(test, null);
        assertTrue(session.exists(test));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test));
        session.close();
    }

    @Test
    public void testTouch() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(), new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path(session.home(), UUID.randomUUID().toString(), Path.FILE_TYPE);
        session.getFeature(Touch.class, new DisabledLoginController()).touch(test);
        assertTrue(session.exists(test));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test));
        assertFalse(session.exists(test));
        session.close();
    }
}
