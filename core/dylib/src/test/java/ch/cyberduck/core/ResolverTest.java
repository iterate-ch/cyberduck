package ch.cyberduck.core;

import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ResolveCanceledException;
import ch.cyberduck.core.exception.ResolveFailedException;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
        assertEquals("fe80:0:0:0:c62c:3ff:fe0b:8670%en0", new Resolver().resolve("fe80::c62c:3ff:fe0b:8670%en0", new DisabledCancelCallback())[0].getHostAddress());
    }

    @Test
    public void testResolvePublicDNSIPv6Only() throws Exception {
        assertEquals("2001:470:a085:999:0:0:0:21", new Resolver().resolve("ftp6.netbsd.org", new DisabledCancelCallback())[0].getHostAddress());
    }

    @Test
    public void testResolvePublicDNSIPv6ForHybrid() throws Exception {
        assertEquals("2600:3c02:0:0:f03c:91ff:fe89:e8b1", new Resolver(true).resolve("intronetworks.cs.luc.edu", new DisabledCancelCallback())[0].getHostAddress());
    }
}
