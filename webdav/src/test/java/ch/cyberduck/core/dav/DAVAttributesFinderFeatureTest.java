package ch.cyberduck.core.dav;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.date.RFC1123DateFormatter;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.github.sardine.DavResource;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Category(IntegrationTest.class)
public class DAVAttributesFinderFeatureTest extends AbstractDAVTest {

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.ch", new Credentials(
            PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path test = new Path(UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));
        final DAVAttributesFinderFeature f = new DAVAttributesFinderFeature(session);
        try {
            f.find(test);
        }
        catch(NotfoundException e) {
            assertEquals("Unexpected response (404 Not Found). Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testFind() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.ch", new Credentials(
            PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path test = new Path("/trunk/LICENSE.txt", EnumSet.of(Path.Type.file));
        final DAVAttributesFinderFeature f = new DAVAttributesFinderFeature(session);
        final PathAttributes attributes = f.find(test);
        assertEquals(35147, attributes.getSize());
        assertNotEquals(-1L, attributes.getModificationDate());
        assertNotNull(attributes.getETag());
        // Test wrong type
        try {
            f.find(new Path("/trunk/LICENSE.txt", EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        session.close();
    }

    @Test
    public void testCustomModified_NotModified() throws Exception {
        final DAVAttributesFinderFeature f = new DAVAttributesFinderFeature(null);
        final DavResource mock = mock(DavResource.class);

        Map<QName, String> map = new HashMap<>();
        final String ts = "Mon, 29 Oct 2018 21:14:06 UTC";
        map.put(DAVTimestampFeature.LAST_MODIFIED_CUSTOM_NAMESPACE, ts);
        map.put(DAVTimestampFeature.LAST_MODIFIED_SERVER_CUSTOM_NAMESPACE, "Thu, 01 Nov 2018 15:31:57 UTC");
        when(mock.getModified()).thenReturn(new DateTime("2018-11-01T15:31:57Z").toDate());
        when(mock.getCustomPropsNS()).thenReturn(map);

        final PathAttributes attrs = f.toAttributes(mock);
        assertEquals(new RFC1123DateFormatter().parse(ts).getTime(), attrs.getModificationDate());
    }

    @Test
    public void testCustomModified_NotSet() {
        final DAVAttributesFinderFeature f = new DAVAttributesFinderFeature(null);
        final DavResource mock = mock(DavResource.class);

        Map<QName, String> map = new HashMap<>();
        final Date modified = new DateTime("2018-11-01T15:31:57Z").toDate();
        when(mock.getModified()).thenReturn(modified);
        when(mock.getCustomPropsNS()).thenReturn(map);

        final PathAttributes attrs = f.toAttributes(mock);
        assertEquals(modified.getTime(), attrs.getModificationDate());
    }

    @Test
    public void testCustomModified_Modified() {
        final DAVAttributesFinderFeature f = new DAVAttributesFinderFeature(null);
        final DavResource mock = mock(DavResource.class);

        Map<QName, String> map = new HashMap<>();
        map.put(DAVTimestampFeature.LAST_MODIFIED_CUSTOM_NAMESPACE, "Mon, 29 Oct 2018 21:14:06 UTC");
        map.put(DAVTimestampFeature.LAST_MODIFIED_SERVER_CUSTOM_NAMESPACE, "Thu, 01 Nov 2018 15:31:57 UTC");
        final Date modified = new DateTime("2018-11-02T15:31:57Z").toDate();
        when(mock.getModified()).thenReturn(modified);
        when(mock.getCustomPropsNS()).thenReturn(map);

        final PathAttributes attrs = f.toAttributes(mock);
        assertEquals(modified.getTime(), attrs.getModificationDate());
    }

    @Test
    public void testFindLock() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
            System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path test = new DAVTouchFeature(session).touch(new Path(new DefaultHomeFinderService(session).find(),
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DAVAttributesFinderFeature f = new DAVAttributesFinderFeature(session);
        assertNull(f.find(test).getLockId());
        final String lockId = new DAVLockFeature(session).lock(test);
        assertNotNull(f.find(test).getLockId());
        new DAVLockFeature(session).unlock(test, lockId);
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}
