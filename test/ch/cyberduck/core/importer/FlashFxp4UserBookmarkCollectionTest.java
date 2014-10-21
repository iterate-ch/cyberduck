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
public class FlashFxp4UserBookmarkCollectionTest extends AbstractTestCase {

    @Test(expected = AccessDeniedException.class)
    public void testParseNotFound() throws Exception {
        new FlashFxp4UserBookmarkCollection().parse(new FinderLocal(System.getProperty("java.io.tmpdir"), "f"));
    }

    @Test
    public void testParse() throws Exception {
        FlashFxpBookmarkCollection c = new FlashFxp4UserBookmarkCollection();
        assertEquals(0, c.size());
        c.parse(LocalFactory.get("test/ch/cyberduck/core/importer/FlashFXP-Sites.dat"));
        assertEquals(4, c.size());
        assertEquals("ftp.intel.com", c.get(1).getHostname());
        assertEquals("ftp.sierra.com", c.get(2).getHostname());
    }
}