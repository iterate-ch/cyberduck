package ch.cyberduck.core.dav;

import ch.cyberduck.core.Scheme;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DAVProtocolTest {

    @Test
    public void testPrefix() {
        assertEquals("ch.cyberduck.core.dav.DAV", new DAVProtocol().getPrefix());
    }

    @Test
    public void testConfigurable() {
        assertTrue(new DAVProtocol().isHostnameConfigurable());
        assertTrue(new DAVProtocol().isPortConfigurable());
    }

    @Test
    public void testSchemes() {
        assertTrue(Arrays.asList(new DAVProtocol().getSchemes()).contains(Scheme.http));
    }
}
