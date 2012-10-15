package ch.cyberduck.core.filter;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NullLocal;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class UploadRegexFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        assertFalse(new UploadRegexFilter().accept(new NullLocal(null, ".DS_Store")));
        assertTrue(new UploadRegexFilter().accept(new NullLocal(null, "f")));
    }
}
