package ch.cyberduck.ui.cocoa.model;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathReferenceFactory;
import ch.cyberduck.ui.cocoa.foundation.NSString;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @version $Id$
 */
public class OutlinePathReferenceTest extends AbstractTestCase {

    @Test
    public void testUnique() throws Exception {
        OutlinePathReference r = new OutlinePathReference(NSString.stringWithString("a"));
        assertEquals(r, new OutlinePathReference(NSString.stringWithString("a")));
        assertEquals(r.unique(), new OutlinePathReference(NSString.stringWithString("a")).unique());
        assertNotSame(r, new OutlinePathReference(NSString.stringWithString("b")));
        assertNotSame(r.unique(), new OutlinePathReference(NSString.stringWithString("b")).unique());
    }

    @Test
    public void testEqualConstructors() throws Exception {
        assertEquals(new OutlinePathReference(NSString.stringWithString("/b")), PathReferenceFactory.createPathReference(
                new NullPath("/b", Path.FILE_TYPE)
        ));
    }
}
