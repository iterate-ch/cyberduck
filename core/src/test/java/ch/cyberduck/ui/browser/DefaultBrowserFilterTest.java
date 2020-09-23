package ch.cyberduck.ui.browser;

import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultBrowserFilterTest {

    @Test
    public void testAccept() {
        final DefaultBrowserFilter f = new DefaultBrowserFilter();
        assertFalse(f.accept(new Path(".f", EnumSet.of(Path.Type.file))));
        assertTrue(f.accept(new Path("f.f", EnumSet.of(Path.Type.file))));
        final Path d = new Path("f.f", EnumSet.of(Path.Type.file));
        d.attributes().setDuplicate(true);
        assertFalse(f.accept(d));
    }

    @Test
    public void testCustomPattern() {
        final DefaultBrowserFilter f = new DefaultBrowserFilter(Pattern.compile("\\..*|~\\$.*"));
        assertFalse(f.accept(new Path(".f", EnumSet.of(Path.Type.file))));
        assertTrue(f.accept(new Path("f", EnumSet.of(Path.Type.file))));
    }
}
