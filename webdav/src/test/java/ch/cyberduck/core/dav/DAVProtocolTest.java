package ch.cyberduck.core.dav;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DAVProtocolTest {

    @Test
    public void testPrefix() {
        assertEquals("ch.cyberduck.core.dav.DAV", new DAVProtocol().getPrefix());
    }
}