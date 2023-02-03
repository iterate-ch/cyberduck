package ch.cyberduck.core;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NavigationTest {

    @Test
    public void testEmpty() {
        Navigation n = new Navigation();
        assertEquals(0, n.getBack().size());
        assertEquals(0, n.getForward().size());
        assertNull(n.back());
        assertNull(n.forward());
    }

    @Test
    public void testBack() {
        Navigation n = new Navigation();
        assertNull(n.back());
        n.add(new Path("a", EnumSet.of(Path.Type.directory)));
        n.add(new Path("b", EnumSet.of(Path.Type.directory)));
        assertEquals("a", n.back().getName());
        assertEquals("b", n.forward().getName());
    }

    @Test
    public void testBackAfterUpAction() {
        Navigation n = new Navigation();
        assertNull(n.back());
        final String home = "/home/boss/test";
        // our current working directory is home.
        n.add(new Path(home, EnumSet.of(Path.Type.directory)));
        //Entering now to "www" directory
        n.add(new Path("www", EnumSet.of(Path.Type.directory)));
        // Click Up button
        // UpButtonClick Action adds the path "/home/boss/test" again to the history
        n.add(new Path(home, EnumSet.of(Path.Type.directory)));
        // Now we are in the home directory. From here traversing to the maildir
        n.add(new Path("maildir", EnumSet.of(Path.Type.directory)));
        // click back button.
        // now our current working directory should be home.
        assertEquals(home, n.back().getAbsolute());

    }
}
