package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class PathReferenceTest {

    @Test
    public void testUnique() throws Exception {
        Path one = new NullPath("a", Path.FILE_TYPE);
        Path second = new NullPath("a", Path.FILE_TYPE);
        assertEquals(one.getReference(), second.getReference());
    }
}
