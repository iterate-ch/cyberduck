package ch.cyberduck.core;

import ch.cyberduck.core.exception.ResolveFailedException;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Category(IntegrationTest.class)
public class ResolverTest {

    @Test
    public void testResolve() throws Exception {
        assertEquals("54.228.253.92", new Resolver().resolve("cyberduck.ch").getHostAddress());
    }

    @Test(expected = ResolveFailedException.class)
    public void testFailure() throws Exception {
        assertNull(new Resolver().resolve("non.cyberduck.ch").getHostAddress());
    }

    @Test
    public void testResolveIPv6Localhost() throws Exception {
        assertEquals("localhost", new Resolver().resolve("::1").getHostName());
        assertEquals("0:0:0:0:0:0:0:1", new Resolver().resolve("::1").getHostAddress());
    }

    @Test
    @Ignore
    public void testResolveLinkLocalZoneIndexInterfaceName() throws Exception {
        assertEquals("andaman.local", new Resolver().resolve("andaman.local").getHostName());
        assertEquals("fe80:0:0:0:c62c:3ff:fe0b:8670%en0", new Resolver().resolve("fe80::c62c:3ff:fe0b:8670%en0").getHostAddress());
    }

    @Test
    public void testResolvePublicDNSIPv6Only() throws Exception {
        assertEquals("2001:470:a085:999:0:0:0:21", new Resolver().resolve("ftp6.netbsd.org").getHostAddress());
    }
}
