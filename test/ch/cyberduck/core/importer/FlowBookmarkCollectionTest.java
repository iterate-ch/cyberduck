package ch.cyberduck.core.importer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.LocalFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class FlowBookmarkCollectionTest extends AbstractTestCase {

    @Test
    public void testParse() {
        FlowBookmarkCollection c = new FlowBookmarkCollection();
        assertEquals(0, c.size());
        c.parse(LocalFactory.createLocal("test/ch/cyberduck/core/importer/com.fivedetails.Bookmarks.plist"));
        assertEquals(3, c.size());
    }
}