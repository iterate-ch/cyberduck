package ch.cyberduck.core.filter;

import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DownloadRegexFilterTest {

    @Test
    public void testAccept() throws Exception {
        final Pattern pattern = Pattern.compile(".*~\\..*|\\.DS_Store|\\.svn|CVS|RCS|SCCS|\\.git|\\.bzr|\\.bzrignore|\\.bzrtags|\\.hg|\\.hgignore|\\.hgtags|_darcs|\\.file-segments");
        assertFalse(new DownloadRegexFilter(pattern).accept(new Path(".DS_Store", EnumSet.of(Path.Type.file))));
        assertTrue(new DownloadRegexFilter(pattern).accept(new Path("f", EnumSet.of(Path.Type.file))));
        assertTrue(new DownloadRegexFilter(Pattern.compile("")).accept(new Path("f", EnumSet.of(Path.Type.file))));
        assertTrue(new DownloadRegexFilter(Pattern.compile("")).accept(new Path("f", EnumSet.of(Path.Type.file))));
    }
}