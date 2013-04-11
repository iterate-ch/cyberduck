package ch.cyberduck.core;

import ch.cyberduck.core.local.LocalFactory;
import ch.cyberduck.core.serializer.ProfileReaderFactory;

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
        assertEquals(Protocol.S3_SSL, profile.getProtocol());
        assertNotSame(Protocol.S3_SSL.getDefaultHostname(), profile.getDefaultHostname());
        assertEquals(Protocol.S3_SSL.getScheme(), profile.getScheme());
        assertEquals("eucalyptus", profile.getProvider());

    }

    @Test
    public void testProvider() throws Exception {
        final Profile profile = ProfileReaderFactory.get().read(
                LocalFactory.createLocal("profiles/HP Cloud Object Storage (US East).cyberduckprofile")
        );
        assertEquals(Protocol.SWIFT, profile.getProtocol());
        assertNotSame(Protocol.CLOUDFILES.getDefaultHostname(), profile.getDefaultHostname());
        assertEquals(Scheme.https, profile.getScheme());
        assertNotNull(profile.getProvider());

    }
}