package ch.cyberduck.core;

import ch.cyberduck.core.openstack.CloudfilesProtocol;
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
                LocalFactory.createLocal("profiles/Eucalyptus Walrus S3.cyberduckprofile")
        );
        assertEquals(Protocol.Type.s3, profile.getType());
        assertEquals(new S3Protocol(), profile.getProtocol());
        assertNotSame(new S3Protocol().getDefaultHostname(), profile.getDefaultHostname());
        assertEquals(new S3Protocol().getScheme(), profile.getScheme());
        assertEquals("eucalyptus", profile.getProvider());

    }

    @Test
    public void testProvider() throws Exception {
        final Profile profile = ProfileReaderFactory.get().read(
                LocalFactory.createLocal("profiles/HP Cloud Object Storage.cyberduckprofile")
        );
        assertEquals(Protocol.Type.swift, profile.getType());
        assertEquals(new SwiftProtocol(), profile.getProtocol());
        assertNotSame(new CloudfilesProtocol().getDefaultHostname(), profile.getDefaultHostname());
        assertEquals(Scheme.https, profile.getScheme());
        assertNotNull(profile.disk());
        assertEquals(profile.disk(), profile.disk());
        assertFalse(profile.disk().equals(new SwiftProtocol().disk()));
        assertNotNull(profile.getProvider());
    }
}