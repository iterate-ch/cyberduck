package ch.cyberduck.core;

import org.junit.Assert;
import org.junit.Test;

import java.net.UnknownHostException;

/**
 * @version $Id:$
 */
public class ResolverTest {

    @Test
    public void testResolve() throws Exception {
        Resolver resolver = new Resolver("cyberduck.ch");
        Assert.assertEquals("80.74.154.52", resolver.resolve().getHostAddress());
    }

    @Test(expected = UnknownHostException.class)
    public void testFailure() throws Exception {
        Resolver resolver = new Resolver("non.cyberduck.ch");
        Assert.assertNull(resolver.resolve().getHostAddress());
    }
}
