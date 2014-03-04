package ch.cyberduck.core;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class HiddenFilesPathFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        assertFalse(new HiddenFilesPathFilter().accept(new Path(".f", EnumSet.of(Path.Type.file))));
        assertTrue(new HiddenFilesPathFilter().accept(new Path("f.f", EnumSet.of(Path.Type.file))));
        final Path d = new Path("f.f", EnumSet.of(Path.Type.file));
        d.attributes().setDuplicate(true);
        assertFalse(new HiddenFilesPathFilter().accept(d));
    }
}
