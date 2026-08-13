package ch.cyberduck.core;

import ch.cyberduck.core.exception.ResolveFailedException;
import ch.cyberduck.core.threading.CancelCallback;

import org.junit.Test;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import static org.junit.Assert.*;

public class ResolverTest {

    @Test
    public void testResolveLocalhost() throws Exception {
        for(String hostname : new String[]{"localhost", "localhost.", "LocalHost", "foo.localhost", "foo.localhost.", "a.b.localhost."}) {
            final InetAddress[] resolved = new Resolver(false).resolve(hostname, CancelCallback.noop);
            assertEquals(2, resolved.length);
            assertTrue(hostname, resolved[0] instanceof Inet4Address);
            assertTrue(hostname, resolved[1] instanceof Inet6Address);
            for(InetAddress address : resolved) {
                assertTrue(hostname, address.isLoopbackAddress());
                assertEquals(hostname, hostname, address.getHostName());
            }
        }
    }

    @Test
    public void testResolveLocalhostPreferIPv6() throws Exception {
        final InetAddress[] resolved = new Resolver(true).resolve("localhost.", CancelCallback.noop);
        assertEquals(2, resolved.length);
        assertTrue(resolved[0] instanceof Inet6Address);
        assertTrue(resolved[1] instanceof Inet4Address);
    }

    @Test
    public void testNotLocalhost() throws Exception {
        // Reserved for names that are not intended to be resolved (RFC 6761 (6.4))
        for(String hostname : new String[]{"localhost.invalid", "notlocalhost.invalid", "localhosts.invalid"}) {
            try {
                new Resolver(false).resolve(hostname, CancelCallback.noop);
                fail(String.format("Expected failure resolving %s", hostname));
            }
            catch(ResolveFailedException e) {
                // Not answered with the loopback address
            }
        }
    }
}
