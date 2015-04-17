package ch.cyberduck.core.bonjour;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.sftp.SFTPProtocol;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class RendezvousCollectionTest extends AbstractTestCase {

    @Test
    public void testAdd() throws Exception {
        final RendezvousCollection c = RendezvousCollection.defaultCollection();
        assertFalse(c.allowsAdd());
        assertFalse(c.allowsDelete());
        assertFalse(c.allowsEdit());
        final Rendezvous rendezvous = RendezvousFactory.instance();
        rendezvous.init();
        final Host h = new Host(new SFTPProtocol(), "h");
        ((AbstractRendezvous) rendezvous).add("h_sftp", h);
        assertEquals(1, c.size());
        assertEquals(h, c.get(0));
        assertNotNull(c.get(0).getUuid());
        ((AbstractRendezvous) rendezvous).remove("h_sftp");
        assertEquals(0, c.size());
        assertTrue(c.isEmpty());
    }
}
