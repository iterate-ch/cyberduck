package ch.cyberduck.core.nextcloud;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.date.RFC1123DateFormatter;
import ch.cyberduck.core.dav.DAVDeleteFeature;
import ch.cyberduck.core.dav.DAVDirectoryFeature;
import ch.cyberduck.core.dav.DAVLockFeature;
import ch.cyberduck.core.dav.DAVTimestampFeature;
import ch.cyberduck.core.dav.DAVTouchFeature;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
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

import com.github.sardine.DavResource;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Category(IntegrationTest.class)
public class NextcloudAttributesFinderFeatureTest extends AbstractNextcloudTest {

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(),
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final NextcloudAttributesFinderFeature f = new NextcloudAttributesFinderFeature(session);
        try {
            f.find(test);
        }
        catch(NotfoundException e) {
            assertEquals("Unexpected response (404 Not Found). Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testFindFile() throws Exception {
        final Path test = new DAVTouchFeature(new NextcloudWriteFeature(session), new NextcloudAttributesFinderFeature(session)).touch(new Path(new DefaultHomeFinderService(session).find(),
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(test.attributes().getFileId());
        final NextcloudAttributesFinderFeature f = new NextcloudAttributesFinderFeature(session);
        final PathAttributes attributes = f.find(test);
        assertEquals(test.attributes().getFileId(), attributes.getFileId());
        assertEquals(0L, attributes.getSize());
        assertNotEquals(-1L, attributes.getModificationDate());
        assertNotNull(attributes.getETag());
        assertNotNull(attributes.getFileId());
        // Test wrong type
        try {
            f.find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        finally {
            new DAVDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    @Test
    public void testFindDirectory() throws Exception {
        final Path test = new DAVDirectoryFeature(session, new NextcloudAttributesFinderFeature(session)).mkdir(new Path(new DefaultHomeFinderService(session).find(),
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertNotNull(test.attributes().getFileId());
        final NextcloudAttributesFinderFeature f = new NextcloudAttributesFinderFeature(session);
        final PathAttributes attributes = f.find(test);
        assertEquals(test.attributes().getFileId(), attributes.getFileId());
        assertEquals(-1, attributes.getSize());
        assertNotEquals(-1L, attributes.getModificationDate());
        assertNotNull(attributes.getETag());
        // Test wrong type
        try {
            f.find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.file)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
    }

    @Test
    public void testCustomModified_NotModified() throws Exception {
        final NextcloudAttributesFinderFeature f = new NextcloudAttributesFinderFeature(null);
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
        final NextcloudAttributesFinderFeature f = new NextcloudAttributesFinderFeature(null);
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
        final NextcloudAttributesFinderFeature f = new NextcloudAttributesFinderFeature(null);
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
    public void testCustomModified_Epoch() {
        final NextcloudAttributesFinderFeature f = new NextcloudAttributesFinderFeature(null);
        final DavResource mock = mock(DavResource.class);

        Map<QName, String> map = new HashMap<>();
        map.put(DAVTimestampFeature.LAST_MODIFIED_CUSTOM_NAMESPACE, "Thu, 01 Jan 1970 00:00:00 UTC");
        map.put(DAVTimestampFeature.LAST_MODIFIED_SERVER_CUSTOM_NAMESPACE, "Thu, 02 Nov 2018 15:31:57 UTC");
        final Date modified = new DateTime("2018-11-02T15:31:57Z").toDate();
        when(mock.getModified()).thenReturn(modified);
        when(mock.getCustomPropsNS()).thenReturn(map);

        final PathAttributes attrs = f.toAttributes(mock);
        assertEquals(modified.getTime(), attrs.getModificationDate());
    }

    @Test(expected = InteroperabilityException.class)
    public void testFindLock() throws Exception {
        final Path test = new DAVTouchFeature(new NextcloudWriteFeature(session), new NextcloudAttributesFinderFeature(session)).touch(new Path(new DefaultHomeFinderService(session).find(),
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final NextcloudAttributesFinderFeature f = new NextcloudAttributesFinderFeature(session);
        assertNull(f.find(test).getLockId());
        final String lockId = new DAVLockFeature(session).lock(test);
        assertNotNull(f.find(test).getLockId());
        new DAVLockFeature(session).unlock(test, lockId);
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}
