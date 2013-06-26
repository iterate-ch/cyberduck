package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class ProtocolFactoryTest extends AbstractTestCase {

    @Test
    public void testRegister() throws Exception {
        assertFalse(ProtocolFactory.getKnownProtocols().isEmpty());
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