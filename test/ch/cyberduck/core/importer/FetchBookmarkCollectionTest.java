package ch.cyberduck.core.importer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.local.FinderLocal;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class FetchBookmarkCollectionTest extends AbstractTestCase {

    @Test(expected = AccessDeniedException.class)
    public void testParseNotFound() throws Exception {
        new FetchBookmarkCollection().parse(new FinderLocal(System.getProperty("java.io.tmpdir"), "f"));
    }

    @Test
    public void testGetFile() throws Exception {
        FetchBookmarkCollection c = new FetchBookmarkCollection();
        assertEquals(0, c.size());
        c.parse(new Local("test/ch/cyberduck/core/importer/com.fetchsoftworks.Fetch.Shortcuts.plist"));
        assertEquals(2, c.size());
    }
}
