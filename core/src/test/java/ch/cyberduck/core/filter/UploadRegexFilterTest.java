package ch.cyberduck.core.filter;

import ch.cyberduck.core.NullLocal;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UploadRegexFilterTest {

    @Test
    public void testAccept() throws Exception {
        final Pattern pattern = Pattern.compile(".*~\\..*|\\.DS_Store|\\.svn|CVS");
        assertFalse(new UploadRegexFilter(pattern).accept(new NullLocal(".DS_Store")));
        assertTrue(new UploadRegexFilter(pattern).accept(new NullLocal("f")));
        assertTrue(new UploadRegexFilter(Pattern.compile("")).accept(new NullLocal("f")));
        assertTrue(new UploadRegexFilter(Pattern.compile("")).accept(new NullLocal(".DS_Store")));
    }
}
