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
        assertEquals(new NSObjectPathReference(NSString.stringWithString("/b")), new NSObjectPathReference(
                new Path("/b", Path.FILE_TYPE)
        ));
    }
}
