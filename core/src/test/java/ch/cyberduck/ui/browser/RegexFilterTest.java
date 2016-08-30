package ch.cyberduck.ui.browser;

import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegexFilterTest {

    @Test
    public void testAccept() throws Exception {
        assertFalse(new RegexFilter().accept(new Path(".f", EnumSet.of(Path.Type.file))));
        assertTrue(new RegexFilter().accept(new Path("f.f", EnumSet.of(Path.Type.file))));
        final Path d = new Path("f.f", EnumSet.of(Path.Type.file));
        d.attributes().setDuplicate(true);
        assertFalse(new RegexFilter().accept(d));
    }
}
