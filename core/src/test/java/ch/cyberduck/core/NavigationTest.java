package ch.cyberduck.core;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NavigationTest {

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
        n.add(new Path("a", EnumSet.of(Path.Type.directory)));
        n.add(new Path("b", EnumSet.of(Path.Type.directory)));
        assertEquals("a", n.back().getName());
        assertEquals("b", n.forward().getName());
    }
}
