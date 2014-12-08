package ch.cyberduck.core.filter;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.test.NullLocal;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class UploadRegexFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        assertFalse(new UploadRegexFilter().accept(new NullLocal(".DS_Store")));
        assertTrue(new UploadRegexFilter().accept(new NullLocal("f")));
    }
}
