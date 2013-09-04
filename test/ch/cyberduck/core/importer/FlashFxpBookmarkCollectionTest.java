package ch.cyberduck.core.importer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class FlashFxpBookmarkCollectionTest extends AbstractTestCase {

    @Test
    public void testParse() {
        FlashFxpBookmarkCollection c = new FlashFxpBookmarkCollection() {
            @Override
            public Local getFile() {
                return null;
            }

            @Override
            public String getBundleIdentifier() {
                return null;
            }
        };
        assertEquals(0, c.size());
        c.parse(LocalFactory.createLocal("test/ch/cyberduck/core/importer/FlashFXP-Sites.dat"));
        assertEquals(4, c.size());
        assertEquals("ftp.intel.com", c.get(1).getHostname());
        assertEquals("ftp.sierra.com", c.get(2).getHostname());
    }
}