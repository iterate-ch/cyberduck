package ch.cyberduck.core;

import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ResolveCanceledException;
import ch.cyberduck.core.exception.ResolveFailedException;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.Inet4Address;
import java.net.Inet6Address;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class ResolverTest {

    @Test
    public void testResolve() throws Exception {
        assertEquals("52.31.8.231", new Resolver().resolve("cyberduck.io", new DisabledCancelCallback())[0].getHostAddress());
    }

    @Test
    public void testFailure() throws Exception {
        final Resolver resolver = new Resolver();
        try {
            resolver.resolve("non.cyberduck.io", new DisabledCancelCallback());
            fail();
        }
        catch(ResolveFailedException e) {
            //
        }
        assertNotNull(resolver.resolve("cyberduck.io", new DisabledCancelCallback())[0].getHostAddress());
    }

    @Test
    public void testCancel() throws Exception {
        final Resolver resolver = new Resolver();
        try {
            resolver.resolve("non.cyberduck.io", new CancelCallback() {
                @Override
                public void verify() throws ConnectionCanceledException {
                    throw new ConnectionCanceledException();
                }
            });
            fail();
        }
        catch(ResolveCanceledException e) {
            //
        }
        assertNotNull(resolver.resolve("cyberduck.io", new DisabledCancelCallback())[0].getHostAddress());
    }

    @Test
    public void testResolveIPv6Localhost() throws Exception {
        assertEquals("localhost", new Resolver().resolve("::1", new DisabledCancelCallback())[0].getHostName());
        assertEquals("0:0:0:0:0:0:0:1", new Resolver().resolve("::1", new DisabledCancelCallback())[0].getHostAddress());
    }

    @Test
    @Ignore
    public void testResolveLinkLocalZoneIndexInterfaceName() throws Exception {
        assertEquals("andaman.local", new Resolver().resolve("andaman.local", new DisabledCancelCallback())[0].getHostName());
        assertEquals("fe80:0:0:0:c62c:3ff:fe0b:8670%en0", new Resolver()
                .resolve("fe80::c62c:3ff:fe0b:8670%en0", new DisabledCancelCallback())[0].getHostAddress());
    }

    @Test
    public void testResolvePublicDnsIPv6Only() throws Exception {
        assertEquals("2001:470:a085:999:0:0:0:21", new Resolver()
                .resolve("ftp6.netbsd.org", new DisabledCancelCallback())[0].getHostAddress());
    }

    @Test
    public void testResolvePublicPreferIpv6() throws Exception {
        assertTrue("2600:3c02:0:0:f03c:91ff:fe89:e8b1", new Resolver(true)
                .resolve("intronetworks.cs.luc.edu", new DisabledCancelCallback())[0] instanceof Inet6Address);
        assertTrue("2620:100:6025:14:0:0:a27d:450e", new Resolver(true)
                .resolve("content.dropboxapi.com", new DisabledCancelCallback())[0] instanceof Inet6Address);
        assertTrue("2a00:1450:400a:803:0:0:0:200a", new Resolver(true)
                .resolve("www.googleapis.com", new DisabledCancelCallback())[0] instanceof Inet6Address);
        assertTrue("2603:1027:1:28:0:0:0:25", new Resolver(true)
                .resolve("login.microsoftonline.com", new DisabledCancelCallback())[0] instanceof Inet6Address);
    }

    @Test
    public void testResolvePublicPreferIpv4() throws Exception {
        assertTrue("23.239.19.84", new Resolver(false)
                .resolve("intronetworks.cs.luc.edu", new DisabledCancelCallback())[0] instanceof Inet4Address);
        assertTrue("162.125.69.14", new Resolver(false)
                .resolve("content.dropboxapi.com", new DisabledCancelCallback())[0] instanceof Inet4Address);
        assertTrue("216.58.215.234", new Resolver(false)
                .resolve("www.googleapis.com", new DisabledCancelCallback())[0] instanceof Inet4Address);
        assertTrue("20.190.181.0", new Resolver(false)
                .resolve("login.microsoftonline.com", new DisabledCancelCallback())[0] instanceof Inet4Address);
    }
}
