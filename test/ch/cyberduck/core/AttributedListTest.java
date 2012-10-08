package ch.cyberduck.core;

import ch.cyberduck.ui.cocoa.foundation.NSString;
import ch.cyberduck.ui.cocoa.model.OutlinePathReference;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class AttributedListTest extends AbstractTestCase {

    @Test
    public void testAdd() throws Exception {
        AttributedList<Path> list = new AttributedList<Path>();
        assertTrue(list.add(new NullPath("/a", Path.DIRECTORY_TYPE)));
        assertTrue(list.contains(new OutlinePathReference(NSString.stringWithString("/a"))));
    }
}
