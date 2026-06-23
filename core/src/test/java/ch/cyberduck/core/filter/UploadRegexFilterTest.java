package ch.cyberduck.core.filter;

import ch.cyberduck.core.NullLocal;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UploadRegexFilterTest {

    @Test
    public void testPattern() {
        final Pattern pattern = Pattern.compile("\\._.*|.*~\\..*|\\.DS_Store|\\.svn|CVS");
        final UploadRegexFilter filter = new UploadRegexFilter(pattern);
        assertFalse(filter.accept(new NullLocal(".DS_Store")));
        assertTrue(filter.accept(new NullLocal("f")));
        assertFalse(filter.accept(new NullLocal("._f")));
        assertTrue(filter.accept(new NullLocal("__init__.py")));
    }

    @Test
    public void testEmptyPattern() {
        final UploadRegexFilter filter = new UploadRegexFilter(Pattern.compile(""));
        assertTrue(filter.accept(new NullLocal("f")));
        assertTrue(filter.accept(new NullLocal(".DS_Store")));
    }
}
