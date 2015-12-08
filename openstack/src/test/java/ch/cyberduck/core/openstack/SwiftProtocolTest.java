package ch.cyberduck.core.openstack;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class SwiftProtocolTest {

    @Test
    public void testPrefix() {
        assertEquals("ch.cyberduck.core.openstack.Swift", new SwiftProtocol().getPrefix());
    }
}