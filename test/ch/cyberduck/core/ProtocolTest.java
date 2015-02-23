package ch.cyberduck.core;

import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.gstorage.GoogleStorageProtocol;
import ch.cyberduck.core.irods.IRODSProtocol;
import ch.cyberduck.core.openstack.SwiftProtocol;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.sftp.SFTPProtocol;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class ProtocolTest extends AbstractTestCase {

    @Test
    public void testEquals() {
        assertNotSame(new FTPProtocol(), new FTPTLSProtocol());
        assertEquals(new FTPProtocol(), new FTPProtocol());
    }

    @Test
    public void testConfigurable() {
        assertTrue(new S3Protocol().isHostnameConfigurable());
        assertFalse(new S3Protocol().isPortConfigurable());
        assertTrue(new SwiftProtocol().isHostnameConfigurable());
        assertTrue(new SwiftProtocol().isPortConfigurable());
        assertTrue(new FTPProtocol().isHostnameConfigurable());
        assertTrue(new FTPProtocol().isPortConfigurable());
        assertTrue(new SFTPProtocol().isHostnameConfigurable());
        assertTrue(new SFTPProtocol().isPortConfigurable());
        assertTrue(new DAVProtocol().isHostnameConfigurable());
        assertTrue(new DAVProtocol().isPortConfigurable());
        assertTrue(new IRODSProtocol().isHostnameConfigurable());
        assertTrue(new IRODSProtocol().isPortConfigurable());

        assertFalse(new GoogleStorageProtocol().isHostnameConfigurable());
        assertFalse(new GoogleStorageProtocol().isPortConfigurable());
    }

    @Test
    public void testIcons() {
        for(Protocol p : ProtocolFactory.getEnabledProtocols()) {
            assertNotNull(p.disk());
            assertNotNull(p.icon());
            assertNotNull(p.getDefaultPort());
            assertNotNull(p.getDefaultHostname());
            assertNotNull(p.getDescription());
            assertNotNull(p.getIdentifier());
        }
    }
}
