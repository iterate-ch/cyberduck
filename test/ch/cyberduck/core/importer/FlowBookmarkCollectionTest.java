package ch.cyberduck.core.importer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.test.Depends;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class FlowBookmarkCollectionTest extends AbstractTestCase {

    @Test(expected = AccessDeniedException.class)
    public void testParseNotFound() throws Exception {
        new FlowBookmarkCollection().parse(new Local(System.getProperty("java.io.tmpdir"), "f"));
    }

    @Test
    public void testParse() throws AccessDeniedException {
        FlowBookmarkCollection c = new FlowBookmarkCollection();
        assertEquals(0, c.size());
        c.parse(new Local("test/ch/cyberduck/core/importer/com.fivedetails.Bookmarks.plist"));
        assertEquals(3, c.size());
    }
}