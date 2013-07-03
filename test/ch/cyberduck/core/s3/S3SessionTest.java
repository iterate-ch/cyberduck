package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Session;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class S3SessionTest extends AbstractTestCase {

    @Test
    public void testFile() {
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, "h"));
        assertFalse(session.isCreateFileSupported(new S3Path(session, null, "/", Path.VOLUME_TYPE)));
        assertTrue(session.isCreateFileSupported(new S3Path(session, new S3Path(session, null, "/", Path.VOLUME_TYPE), "/container", Path.VOLUME_TYPE)));
    }

    @Test
    public void testConnect() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(), new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        assertNotNull(session.open());
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount());
        assertFalse(session.cache().isEmpty());
        assertTrue(session.cache().containsKey(new S3Path(session, "/", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE).getReference()));
        assertNotNull(session.cache().lookup(new S3Path(session, "/test.cyberduck.ch", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE).getReference()));
        assertTrue(session.isConnected());
        session.close();
        assertNull(session.getClient());
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
    }

    @Test
    public void testConnectCnameAnonymous() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, "dist.springframework.org", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final S3Session session = new S3Session(host);
        assertNotNull(session.open());
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount());
        assertTrue(session.isConnected());
        session.close();
        assertNull(session.getClient());
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
    }


    @Test
    public void testConnectBuckenameAnonymous() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, "dist.springframework.org.s3.amazonaws.com", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final S3Session session = new S3Session(host);
        assertNotNull(session.open());
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertNotNull(session.mount());
        assertTrue(session.isConnected());
        session.close();
        assertNull(session.getClient());
        assertFalse(session.isConnected());
        assertEquals(Session.State.closed, session.getState());
    }

    @Test
    public void testFeatures() throws Exception {
        final S3Session aws = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        assertTrue(aws.isVersioningSupported());
        assertTrue(aws.isAnalyticsSupported());
        assertTrue(aws.isChecksumSupported());
        assertTrue(aws.isLifecycleSupported());
        assertTrue(aws.isLocationSupported());
        assertTrue(aws.isRevertSupported());
        final S3Session o = new S3Session(new Host(Protocol.S3_SSL, "o"));
        assertFalse(o.isVersioningSupported());
        assertFalse(o.isAnalyticsSupported());
        assertTrue(o.isChecksumSupported());
        assertFalse(o.isLifecycleSupported());
        assertTrue(o.isLocationSupported());
        assertFalse(o.isRevertSupported());
    }

    @Test
    public void testUri() throws Exception {
        final S3Session aws = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        assertEquals("https://test.cyberduck.ch.s3.amazonaws.com/key", aws.toURL(new S3Path(aws, "/test.cyberduck.ch/key", Path.FILE_TYPE)));
    }

    @Test
    public void testHttpUri() throws Exception {
        final S3Session aws = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        assertEquals("http://test.cyberduck.ch.s3.amazonaws.com/key", aws.toHttpURL(new S3Path(aws, "/test.cyberduck.ch/key", Path.FILE_TYPE)));
    }
}
