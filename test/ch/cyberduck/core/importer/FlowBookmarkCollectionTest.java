package ch.cyberduck.core.importer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.local.FinderLocal;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class FlowBookmarkCollectionTest extends AbstractTestCase {

    @Test(expected = AccessDeniedException.class)
    public void testParseNotFound() throws Exception {
        new FlowBookmarkCollection().parse(new FinderLocal(System.getProperty("java.io.tmpdir"), "f"));
    }

    @Test
    public void testParse() throws AccessDeniedException {
        FlowBookmarkCollection c = new FlowBookmarkCollection();
        assertEquals(0, c.size());
        c.parse(LocalFactory.get("test/ch/cyberduck/core/importer/com.fivedetails.Bookmarks.plist"));
        assertEquals(3, c.size());
    }
}