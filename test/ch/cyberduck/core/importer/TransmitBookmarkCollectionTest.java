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
public class TransmitBookmarkCollectionTest extends AbstractTestCase {

    @Test(expected = AccessDeniedException.class)
    public void testParseNotFound() throws Exception {
        new TransmitBookmarkCollection().parse(new FinderLocal(System.getProperty("java.io.tmpdir"), "f"));
    }

    @Test
    public void testParse() throws Exception {
        TransmitBookmarkCollection c = new TransmitBookmarkCollection();
        assertEquals(0, c.size());
        c.parse(LocalFactory.get("test/ch/cyberduck/core/importer/com.panic.Transmit.plist"));
        assertEquals(1, c.size());
    }
}