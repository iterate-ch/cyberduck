package ch.cyberduck.core;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class RendezvousCollectionTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        NullRendezvous.register();
    }

    @Test
    public void testAdd() throws Exception {
        RendezvousCollection c = RendezvousCollection.defaultCollection();
        assertFalse(c.allowsAdd());
        assertFalse(c.allowsDelete());
        assertFalse(c.allowsEdit());
        final Rendezvous rendezvous = RendezvousFactory.instance();
        final Host h = new Host(Protocol.SFTP, "h");
        ((AbstractRendezvous) rendezvous).add("h_sftp", h);
        assertEquals(1, c.size());
        assertEquals(h, c.get(0));
        assertNotNull(c.get(0).getUuid());
        ((AbstractRendezvous) rendezvous).remove("h_sftp");
        assertEquals(0, c.size());
        assertTrue(c.isEmpty());
    }
}
