package ch.cyberduck.core.openstack;

import ch.cyberduck.core.Protocol;

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
        for(Protocol p : Arrays.asList(new SwiftProtocol())) {
            assertNotNull(p.disk());
            assertNotNull(p.icon());
            assertNotNull(p.getDefaultPort());
            assertNotNull(p.getDefaultHostname());
            assertNotNull(p.getDescription());
            assertNotNull(p.getIdentifier());
        }
    }
}