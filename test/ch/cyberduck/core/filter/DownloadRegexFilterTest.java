package ch.cyberduck.core.filter;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Path;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class DownloadRegexFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        assertFalse(new DownloadRegexFilter().accept(new Path(".DS_Store", Path.FILE_TYPE)));
        assertTrue(new DownloadRegexFilter().accept(new Path("f", Path.FILE_TYPE)));
    }
}