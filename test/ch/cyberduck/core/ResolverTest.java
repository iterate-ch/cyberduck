package ch.cyberduck.core;

import ch.cyberduck.core.exception.ResolveFailedException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @version $Id$
 */
public class ResolverTest extends AbstractTestCase {

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
}
