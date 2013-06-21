package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @version $Id:$
 */
public class NavigationTest extends AbstractTestCase {

    @Test
    public void testEmpty() throws Exception {
        Navigation n = new Navigation();
        assertEquals(0, n.getBack().size());
        assertEquals(0, n.getForward().size());
        assertNull(n.back());
        assertNull(n.forward());
    }

    @Test
    public void testBack() throws Exception {
        Navigation n = new Navigation();
        assertNull(n.back());
        n.add(new NullPath("a", Path.DIRECTORY_TYPE));
        n.add(new NullPath("b", Path.DIRECTORY_TYPE));
        assertEquals("a", n.back().getName());
        assertEquals("b", n.forward().getName());
    }
}
