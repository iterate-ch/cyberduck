package ch.cyberduck.core.openstack;

import ch.cyberduck.core.Scheme;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class SwiftProtocolTest {

    @Test
    public void testPrefix() {
        assertEquals("ch.cyberduck.core.openstack.Swift", new SwiftProtocol().getPrefix());
    }

    @Test
    public void testConfigurable() {
        assertTrue(new SwiftProtocol().isHostnameConfigurable());
        assertTrue(new SwiftProtocol().isPortConfigurable());
    }

    @Test
    public void testIcons() {
        assertNotNull(new SwiftProtocol().disk());
        assertNotNull(new SwiftProtocol().icon());
        assertNotEquals(-1L, new SwiftProtocol().getDefaultPort());
        assertNotNull(new SwiftProtocol().getDefaultHostname());
        assertNotNull(new SwiftProtocol().getDescription());
        assertNotNull(new SwiftProtocol().getIdentifier());
    }

    @Test
    public void testSchemes() {
        assertTrue(Arrays.asList(new SwiftProtocol().getSchemes()).contains(Scheme.https.name()));
    }
}
