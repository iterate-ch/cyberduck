package ch.cyberduck.core.importer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class FilezillaBookmarkCollectionTest extends AbstractTestCase {

    @Test
    public void testParse() throws Exception {
        FilezillaBookmarkCollection c = new FilezillaBookmarkCollection();
        assertEquals(0, c.size());
        c.parse(new Local("test/ch/cyberduck/core/importer/org.filezilla-project.sitemanager.xml"));
        assertEquals(2, c.size());
    }
}