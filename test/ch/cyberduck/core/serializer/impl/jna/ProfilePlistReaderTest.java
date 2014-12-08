package ch.cyberduck.core.serializer.impl.jna;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.DeserializerFactory;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.openstack.SwiftProtocol;
import ch.cyberduck.core.s3.S3LocationFeature;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.test.Depends;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class ProfilePlistReaderTest extends AbstractTestCase {

    @Test
    public void testDeserialize() throws Exception {
        final ProfilePlistReader reader = new ProfilePlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
        final Profile profile = reader.read(
                LocalFactory.get("test/ch/cyberduck/core/serializer/impl/Dropbox.cyberduckprofile")
        );
        assertNull(profile);
    }

    @Test
    public void testAll() throws Exception {
        for(Local l : new Local("profiles").list().filter(new Filter<Local>() {
            @Override
            public boolean accept(final Local file) {
                return file.getName().endsWith(".cyberduckprofile");
            }
        })) {
            final ProfilePlistReader reader = new ProfilePlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
            final Profile profile = reader.read(l);
            assertNotNull(profile);
        }
    }

    @Test
    public void testRegions() throws Exception {
        final ProfilePlistReader reader = new ProfilePlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
        final Profile profile = reader.read(
                LocalFactory.get("test/ch/cyberduck/core/serializer/impl/Custom Regions S3.cyberduckprofile")
        );
        assertNotNull(profile);
        final Set<Location.Name> regions = profile.getRegions();
        assertEquals(2, regions.size());
        assertTrue("custom", regions.contains(new S3LocationFeature.S3Region("custom")));
        assertTrue("custom2", regions.contains(new S3LocationFeature.S3Region("custom2")));
    }

    @Test
    public void testEquals() throws Exception {
        final ProfilePlistReader reader = new ProfilePlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
        final Profile profile = reader.read(
                new Local("profiles/Eucalyptus Walrus S3.cyberduckprofile")
        );
        assertEquals(profile, reader.read(
                new Local("profiles/Eucalyptus Walrus S3.cyberduckprofile")
        ));
        Assert.assertEquals(Protocol.Type.s3, profile.getType());
        assertEquals(new S3Protocol(), profile.getProtocol());
        assertNotSame(new S3Protocol().getDefaultHostname(), profile.getDefaultHostname());
        assertEquals(new S3Protocol().getScheme(), profile.getScheme());
        assertEquals("eucalyptus", profile.getProvider());
    }

    @Test
    public void testEqualsDifferentScheme() throws Exception {
        final ProfilePlistReader reader = new ProfilePlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
        final Profile https = reader.read(
                LocalFactory.get("profiles/Openstack Swift (Swauth).cyberduckprofile")
        );
        final Profile http = reader.read(
                LocalFactory.get("profiles/Openstack Swift (Swauth HTTP).cyberduckprofile")
        );
        assertNotEquals(https, http);
    }

    @Test
    public void testEqualsContexts() throws Exception {
        final ProfilePlistReader reader = new ProfilePlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
        final Profile keystone = reader.read(
                LocalFactory.get("profiles/Openstack Swift (Keystone).cyberduckprofile")
        );
        final Profile swauth = reader.read(
                LocalFactory.get("profiles/Openstack Swift (Swauth).cyberduckprofile")
        );
        assertNotEquals(keystone, swauth);
    }

    @Test
    public void testProviderProfileHPCloud() throws Exception {
        final ProfilePlistReader reader = new ProfilePlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
        final Profile profile = reader.read(
                LocalFactory.get("profiles/HP Cloud Object Storage.cyberduckprofile")
        );
        assertEquals(profile, reader.read(
                LocalFactory.get("profiles/HP Cloud Object Storage.cyberduckprofile")
        ));
        assertEquals(Protocol.Type.swift, profile.getType());
        assertEquals(new SwiftProtocol(), profile.getProtocol());
        assertFalse(profile.isHostnameConfigurable());
        assertFalse(profile.isPortConfigurable());
        assertEquals("swift", profile.getIdentifier());
        assertNotSame("identity.api.rackspacecloud.com", profile.getDefaultHostname());
        Assert.assertEquals(Scheme.https, profile.getScheme());
        assertNotNull(profile.disk());
        assertEquals(profile.disk(), profile.disk());
        assertEquals(profile.icon(), profile.disk());
        assertEquals("Tenant ID:Access Key", profile.getUsernamePlaceholder());
        assertEquals("Secret Key", profile.getPasswordPlaceholder());
        assertEquals(35357, profile.getDefaultPort());
        assertEquals("region-a.geo-1.identity.hpcloudsvc.com", profile.getDefaultHostname());
        assertEquals("/v2.0/tokens", profile.getContext());
        assertFalse(profile.disk().equals(new SwiftProtocol().disk()));
        assertNotNull(profile.getProvider());
    }

    @Test
    public void testProviderProfileS3HTTP() throws Exception {
        final ProfilePlistReader reader = new ProfilePlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
        final Profile profile = reader.read(
                LocalFactory.get("profiles/S3 (HTTP).cyberduckprofile")
        );
        assertEquals(profile, reader.read(
                LocalFactory.get("profiles/S3 (HTTP).cyberduckprofile")
        ));
        assertEquals(Protocol.Type.s3, profile.getType());
        assertEquals(new S3Protocol(), profile.getProtocol());
        assertTrue(profile.isHostnameConfigurable());
        assertTrue(profile.isPortConfigurable());
        assertEquals("s3", profile.getIdentifier());
        assertEquals(Scheme.http, profile.getScheme());
        assertNotNull(profile.disk());
        assertEquals(profile.disk(), profile.disk());
        assertEquals(profile.icon(), profile.disk());
        assertEquals(80, profile.getDefaultPort());
        assertEquals(new S3Protocol().getDefaultHostname(), profile.getDefaultHostname());
        assertTrue(profile.disk().equals(new S3Protocol().disk()));
        assertNotNull(profile.getProvider());
    }

    @Test
    public void testProviderProfileS3HTTPS() throws Exception {
        final ProfilePlistReader reader = new ProfilePlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
        final Profile profile = reader.read(
                LocalFactory.get("profiles/S3 (HTTPS).cyberduckprofile")
        );
        assertEquals(profile, reader.read(
                LocalFactory.get("profiles/S3 (HTTPS).cyberduckprofile")
        ));
        assertEquals(Protocol.Type.s3, profile.getType());
        assertEquals(new S3Protocol(), profile.getProtocol());
        assertTrue(profile.isHostnameConfigurable());
        assertTrue(profile.isPortConfigurable());
        assertEquals("s3", profile.getIdentifier());
        assertEquals(Scheme.https, profile.getScheme());
        assertNotNull(profile.disk());
        assertEquals(profile.disk(), profile.disk());
        assertEquals(profile.icon(), profile.disk());
        assertEquals(443, profile.getDefaultPort());
        assertEquals(new S3Protocol().getDefaultHostname(), profile.getDefaultHostname());
        assertTrue(profile.disk().equals(new S3Protocol().disk()));
        assertNotNull(profile.getProvider());
    }
}
