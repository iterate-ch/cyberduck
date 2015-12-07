package ch.cyberduck.core;

import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.irods.IRODSProtocol;
import ch.cyberduck.core.openstack.SwiftProtocol;
import ch.cyberduck.core.sftp.SFTPProtocol;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class ProtocolFactoryTest extends AbstractTestCase {

    @Test
    public void testRegister() throws Exception {
        assertFalse(ProtocolFactory.getEnabledProtocols().isEmpty());
    }

    @Test
    public void testForName() throws Exception {
        assertEquals(new FTPProtocol(), ProtocolFactory.forName("ftp"));
        assertEquals(new SFTPProtocol(), ProtocolFactory.forName("sftp"));
        assertEquals(new SwiftProtocol(), ProtocolFactory.forName("swift"));
        assertEquals(new SFTPProtocol(), ProtocolFactory.forName(String.valueOf(new SFTPProtocol().hashCode())));
        assertEquals(new IRODSProtocol(), ProtocolFactory.forName("irods"));
        assertEquals(null, ProtocolFactory.forName(String.valueOf("unknown")));
    }

    @Test
    public void testForScheme() throws Exception {
        assertEquals(new DAVProtocol(), ProtocolFactory.forScheme("http"));
        assertEquals(new DAVSSLProtocol(), ProtocolFactory.forScheme("https"));
        assertEquals(new FTPProtocol(), ProtocolFactory.forScheme("ftp"));
        assertEquals(new FTPTLSProtocol(), ProtocolFactory.forScheme("ftps"));
        assertNull(ProtocolFactory.forScheme("irods"));
    }

    @Test
    public void testUrl() throws Exception {
        assertTrue(Scheme.isURL("ftp://h.name"));
        assertTrue(Scheme.isURL("ftps://h.name"));
        assertTrue(Scheme.isURL("sftp://h.name"));
        assertTrue(Scheme.isURL("http://h.name"));
        assertTrue(Scheme.isURL("https://h.name"));
        assertFalse(Scheme.isURL("irods://h.name"));
        assertFalse(Scheme.isURL("h.name"));
    }

    @Test
    public void testDeprecated() throws Exception {
        assertEquals(new SwiftProtocol(), ProtocolFactory.forName("cf"));
    }
}