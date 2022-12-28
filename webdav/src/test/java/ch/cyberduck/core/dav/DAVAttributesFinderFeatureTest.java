package ch.cyberduck.core.dav;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.cryptomator.features.CryptoAttributesFeature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.date.RFC1123DateFormatter;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.xml.namespace.QName;
import java.util.Arrays;
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
@RunWith(Parameterized.class)
public class DAVAttributesFinderFeatureTest extends AbstractDAVTest {

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final Path test = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DAVAttributesFinderFeature f = new DAVAttributesFinderFeature(session);
        try {
            f.find(test);
        }
        catch(NotfoundException e) {
            assertTrue(StringUtils.startsWith(e.getDetail(), "Unexpected response"));
            throw e;
        }
    }

    @Test
    public void testFindFile() throws Exception {
        final Path test = new DAVTouchFeature(session).touch(new Path(new DefaultHomeFinderService(session).find(),
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DAVAttributesFinderFeature f = new DAVAttributesFinderFeature(session);
        final PathAttributes attributes = f.find(test);
        assertEquals(0L, attributes.getSize());
        assertNotEquals(-1L, attributes.getModificationDate());
        assertNotNull(attributes.getETag());
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
        final Path test = new DAVDirectoryFeature(session).mkdir(new Path(new DefaultHomeFinderService(session).find(),
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final DAVAttributesFinderFeature f = new DAVAttributesFinderFeature(session);
        final PathAttributes attributes = f.find(test);
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
    public void testCustomModified_Epoch() {
        final DAVAttributesFinderFeature f = new DAVAttributesFinderFeature(null);
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

    @Test
    public void testFindLock() throws Exception {
        final Path test = new DAVTouchFeature(session).touch(new Path(new DefaultHomeFinderService(session).find(),
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DAVAttributesFinderFeature f = new DAVAttributesFinderFeature(session);
        assertNull(f.find(test).getLockId());
        final String lockId = new DAVLockFeature(session).lock(test);
        assertNotNull(f.find(test).getLockId());
        try {
            new DAVLockFeature(session).unlock(test, lockId);
        }
        catch(InteroperabilityException e) {
            // No lock support
        }
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindDefaultAttributesFinderCryptomator() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), new DisabledPasswordStore(), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final Path test = new CryptoTouchFeature<>(session, new DAVTouchFeature(session), new DAVWriteFeature(session), cryptomator).touch(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        test.attributes().setSize(0L);
        final PathAttributes attributes = new CryptoAttributesFeature(session, new DefaultAttributesFinderFeature(session), cryptomator).find(test);
        assertNotNull(attributes);
        assertEquals(0L, attributes.getSize());
        cryptomator.getFeature(session, Delete.class, new DAVDeleteFeature(session)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindNoPropfind() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "update.cyberduck.io");
        final DAVSession session = new DAVSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final DAVAttributesFinderFeature f = new DAVAttributesFinderFeature(session);
        final Path file = new Path("/robots.txt", EnumSet.of(Path.Type.file));
        final Attributes attributes = f.find(file);
        assertNotNull(attributes);
    }
}
