package ch.cyberduck.core.filter;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class DownloadRegexFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        assertFalse(new DownloadRegexFilter().accept(new Path(".DS_Store", EnumSet.of(Path.Type.file))));
        assertTrue(new DownloadRegexFilter().accept(new Path("f", EnumSet.of(Path.Type.file))));
    }
}