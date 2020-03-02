package ch.cyberduck.ui.browser;

import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegexFilterTest {

    @Test
    public void testAccept() {
        final RegexFilter f = new RegexFilter();
        assertFalse(f.accept(new Path(".f", EnumSet.of(Path.Type.file))));
        assertTrue(f.accept(new Path("f.f", EnumSet.of(Path.Type.file))));
        final Path d = new Path("f.f", EnumSet.of(Path.Type.file));
        d.attributes().setDuplicate(true);
        assertFalse(f.accept(d));
    }

    @Test
    public void testCustomPattern() {
        final RegexFilter f = new RegexFilter(Pattern.compile("\\..*|~\\$.*"));
        assertFalse(f.accept(new Path(".f", EnumSet.of(Path.Type.file))));
        assertTrue(f.accept(new Path("f", EnumSet.of(Path.Type.file))));
    }
}
