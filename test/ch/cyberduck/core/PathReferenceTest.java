package ch.cyberduck.core;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class PathReferenceTest {

    @BeforeClass
    public static void register() {
        NSObjectPathReference.register();
    }

    @Test
    public void testUnique() throws Exception {
        Path one = new Path("a", Path.FILE_TYPE);
        Path second = new Path("a", Path.FILE_TYPE);
        assertEquals(one.getReference(), second.getReference());
    }
}
