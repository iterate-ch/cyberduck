package ch.cyberduck.core;

import org.junit.Assert;
import org.junit.Test;

import java.net.UnknownHostException;

/**
 * @version $Id$
 */
public class ResolverTest {

    @Test
    public void testResolve() throws Exception {
        Resolver resolver = new Resolver();
        Assert.assertEquals("54.228.253.92", resolver.resolve("cyberduck.ch").getHostAddress());
    }

    @Test(expected = UnknownHostException.class)
    public void testFailure() throws Exception {
        Resolver resolver = new Resolver();
        Assert.assertNull(resolver.resolve("non.cyberduck.ch").getHostAddress());
    }
}
