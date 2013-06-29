package ch.cyberduck.core.importer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.local.LocalFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class FetchBookmarkCollectionTest extends AbstractTestCase {

    @Test
    public void testGetFile() throws Exception {
        FetchBookmarkCollection c = new FetchBookmarkCollection();
        assertEquals(0, c.size());
        c.parse(LocalFactory.createLocal("test/ch/cyberduck/core/importer/com.fetchsoftworks.Fetch.Shortcuts.plist"));
        assertEquals(2, c.size());
    }
}
