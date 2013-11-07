package ch.cyberduck.core.importer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.LocalFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class FireFtpBookmarkCollectionTest extends AbstractTestCase {

    @Test
    public void testParse() throws Exception {
        FireFtpBookmarkCollection c = new FireFtpBookmarkCollection();
        assertEquals(0, c.size());
        c.parse(LocalFactory.createLocal("test/ch/cyberduck/core/importer/org.mozdev.fireftp"));
        assertEquals(1, c.size());
    }
}
