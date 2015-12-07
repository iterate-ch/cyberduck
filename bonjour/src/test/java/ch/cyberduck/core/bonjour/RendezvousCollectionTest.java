package ch.cyberduck.core.bonjour;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.sftp.SFTPProtocol;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class RendezvousCollectionTest {

    @Test
    public void testAdd() throws Exception {
        final Rendezvous bonjour = new Rendezvous() {
            @Override
            public void init() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void quit() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addListener(final RendezvousListener listener) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void removeListener(final RendezvousListener listener) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int numberOfServices() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Host getService(final int index) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Iterator<Host> iterator() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getDisplayedName(final String identifier) {
                throw new UnsupportedOperationException();
            }
        };
        final RendezvousCollection c = new RendezvousCollection(bonjour);
        assertFalse(c.allowsAdd());
        assertFalse(c.allowsDelete());
        assertFalse(c.allowsEdit());
        bonjour.init();
        final Host h = new Host(new SFTPProtocol(), "h");
        ((AbstractRendezvous) bonjour).add("h_sftp", h);
        assertEquals(1, c.size());
        assertEquals(h, c.get(0));
        assertNotNull(c.get(0).getUuid());
        ((AbstractRendezvous) bonjour).remove("h_sftp");
        assertEquals(0, c.size());
        assertTrue(c.isEmpty());
    }
}
