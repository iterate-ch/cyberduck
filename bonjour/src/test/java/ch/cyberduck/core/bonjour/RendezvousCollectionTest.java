package ch.cyberduck.core.bonjour;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class RendezvousCollectionTest {

    @Test
    public void testAdd() throws Exception {
        final AbstractRendezvous bonjour = new AbstractRendezvous(new ProtocolFactory(Collections.singleton(new TestProtocol(Scheme.sftp)))) {
        };
        final RendezvousCollection c = new RendezvousCollection(bonjour);
        assertFalse(c.allowsAdd());
        assertFalse(c.allowsDelete());
        assertFalse(c.allowsEdit());
        bonjour.init();
        final Host h = new Host(new TestProtocol(Scheme.sftp), "h");
        bonjour.add("h_sftp", h);
        assertEquals(1, c.size());
        assertEquals(h, c.get(0));
        assertNotNull(c.get(0).getUuid());
        bonjour.remove("h_sftp");
        assertEquals(0, c.size());
        assertTrue(c.isEmpty());
    }
}
