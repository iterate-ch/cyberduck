package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class ProtocolFactoryTest extends AbstractTestCase {

    @Test
    public void testRegister() throws Exception {
        assertFalse(ProtocolFactory.getKnownProtocols().isEmpty());
    }

    @Test
    public void testForName() throws Exception {
        assertEquals(Protocol.FTP, ProtocolFactory.forName("ftp"));
        assertEquals(Protocol.SFTP, ProtocolFactory.forName("sftp"));
        assertEquals(Protocol.SWIFT, ProtocolFactory.forName("swift"));
        assertEquals(Protocol.SFTP, ProtocolFactory.forName(String.valueOf(Protocol.SFTP.hashCode())));
    }

    @Test
    public void testForScheme() throws Exception {
        assertEquals(Protocol.WEBDAV, ProtocolFactory.forScheme("http"));
        assertEquals(Protocol.WEBDAV_SSL, ProtocolFactory.forScheme("https"));
        assertEquals(Protocol.FTP, ProtocolFactory.forScheme("ftp"));
        assertEquals(Protocol.FTP_TLS, ProtocolFactory.forScheme("ftps"));
    }

    @Test
    public void testUrl() throws Exception {
        assertTrue(ProtocolFactory.isURL("ftp://h.name"));
        assertTrue(ProtocolFactory.isURL("ftps://h.name"));
        assertTrue(ProtocolFactory.isURL("sftp://h.name"));
        assertTrue(ProtocolFactory.isURL("http://h.name"));
        assertFalse(ProtocolFactory.isURL("h.name"));
    }
}