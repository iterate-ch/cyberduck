package ch.cyberduck.core.dav;

import ch.cyberduck.core.Scheme;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DAVSSLProtocolTest {

    @Test
    public void testPrefix() {
        assertEquals("ch.cyberduck.core.dav.DAV", new DAVSSLProtocol().getPrefix());
    }

    @Test
    public void testSchemes() {
        assertTrue(Arrays.asList(new DAVSSLProtocol().getSchemes()).contains(Scheme.https.name()));
    }
}
