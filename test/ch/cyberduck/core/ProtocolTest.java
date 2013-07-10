package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class ProtocolTest extends AbstractTestCase {

    @Test
    public void testEquals() {
        assertNotSame(Protocol.FTP, Protocol.FTP_TLS);
        assertEquals(Protocol.FTP, Protocol.FTP);
    }

    @Test
    public void testIcons() {
        for(Protocol p : ProtocolFactory.getKnownProtocols()) {
            assertNotNull(p.disk());
            assertNotNull(p.icon());
            assertNotNull(p.getDefaultPort());
            assertNotNull(p.getDefaultHostname());
            assertNotNull(p.getDescription());
            assertNotNull(p.getIdentifier());
        }
    }
}
