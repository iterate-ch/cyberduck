package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class HiddenFilesPathFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        assertFalse(new HiddenFilesPathFilter().accept(new Path(".f", Path.FILE_TYPE)));
        assertTrue(new HiddenFilesPathFilter().accept(new Path("f.f", Path.FILE_TYPE)));
        final Path d = new Path("f.f", Path.FILE_TYPE);
        d.attributes().setDuplicate(true);
        assertFalse(new HiddenFilesPathFilter().accept(d));
    }
}
