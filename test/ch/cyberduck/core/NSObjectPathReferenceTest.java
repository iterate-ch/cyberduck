package ch.cyberduck.core;

import ch.cyberduck.ui.cocoa.foundation.NSString;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @version $Id$
 */
public class NSObjectPathReferenceTest extends AbstractTestCase {

    @Test
    public void testUnique() throws Exception {
        NSObjectPathReference r = new NSObjectPathReference(NSString.stringWithString("a"));
        assertEquals(r, new NSObjectPathReference(NSString.stringWithString("a")));
        assertEquals(r.unique(), new NSObjectPathReference(NSString.stringWithString("a")).unique());
        assertNotSame(r, new NSObjectPathReference(NSString.stringWithString("b")));
        assertNotSame(r.unique(), new NSObjectPathReference(NSString.stringWithString("b")).unique());
    }

    @Test
    public void testEqualConstructors() throws Exception {
        assertEquals(new NSObjectPathReference(NSString.stringWithString("1-/b")), new NSObjectPathReference(
                new Path("/b", Path.FILE_TYPE)
        ));
        assertEquals(new NSObjectPathReference(NSString.stringWithString("6-/d")), new NSObjectPathReference(
                new Path("/d", Path.DIRECTORY_TYPE | Path.SYMBOLIC_LINK_TYPE)
        ));
    }
}
