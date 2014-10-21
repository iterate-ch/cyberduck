package ch.cyberduck.core;

import ch.cyberduck.core.openstack.SwiftProtocol;
import ch.cyberduck.core.s3.S3Protocol;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class ProfileTest extends AbstractTestCase {

    @Test
    public void testEquals() throws Exception {
        final Profile profile = ProfileReaderFactory.get().read(
                LocalFactory.get("profiles/Eucalyptus Walrus S3.cyberduckprofile")
        );
        assertEquals(profile, ProfileReaderFactory.get().read(
                LocalFactory.get("profiles/Eucalyptus Walrus S3.cyberduckprofile")
        ));
        assertEquals(Protocol.Type.s3, profile.getType());
        assertEquals(new S3Protocol(), profile.getProtocol());
        assertNotSame(new S3Protocol().getDefaultHostname(), profile.getDefaultHostname());
        assertEquals(new S3Protocol().getScheme(), profile.getScheme());
        assertEquals("eucalyptus", profile.getProvider());
    }

    @Test
    public void testEqualsDifferentScheme() throws Exception {
        final Profile https = ProfileReaderFactory.get().read(
                LocalFactory.get("profiles/Openstack Swift (Swauth).cyberduckprofile")
        );
        final Profile http = ProfileReaderFactory.get().read(
                LocalFactory.get("profiles/Openstack Swift (Swauth HTTP).cyberduckprofile")
        );
        assertNotEquals(https, http);
    }

    @Test
    public void testEqualsContexts() throws Exception {
        final Profile keystone = ProfileReaderFactory.get().read(
                LocalFactory.get("profiles/Openstack Swift (Keystone).cyberduckprofile")
        );
        final Profile swauth = ProfileReaderFactory.get().read(
                LocalFactory.get("profiles/Openstack Swift (Swauth).cyberduckprofile")
        );
        assertNotEquals(keystone, swauth);
    }

    @Test
    public void testProviderProfileHPCloud() throws Exception {
        final Profile profile = ProfileReaderFactory.get().read(
                LocalFactory.get("profiles/HP Cloud Object Storage.cyberduckprofile")
        );
        assertEquals(profile, ProfileReaderFactory.get().read(
                LocalFactory.get("profiles/HP Cloud Object Storage.cyberduckprofile")
        ));
        assertEquals(Protocol.Type.swift, profile.getType());
        assertEquals(new SwiftProtocol(), profile.getProtocol());
        assertFalse(profile.isHostnameConfigurable());
        assertFalse(profile.isPortConfigurable());
        assertEquals("swift", profile.getIdentifier());
        assertNotSame("identity.api.rackspacecloud.com", profile.getDefaultHostname());
        assertEquals(Scheme.https, profile.getScheme());
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
        final Profile profile = ProfileReaderFactory.get().read(
                LocalFactory.get("profiles/S3 (HTTP).cyberduckprofile")
        );
        assertEquals(profile, ProfileReaderFactory.get().read(
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
        final Profile profile = ProfileReaderFactory.get().read(
                LocalFactory.get("profiles/S3 (HTTPS).cyberduckprofile")
        );
        assertEquals(profile, ProfileReaderFactory.get().read(
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