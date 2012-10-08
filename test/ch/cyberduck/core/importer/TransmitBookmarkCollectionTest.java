package ch.cyberduck.core.importer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.LocalFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class TransmitBookmarkCollectionTest extends AbstractTestCase {

    @Test
    public void testParse() throws Exception {
        TransmitBookmarkCollection c = new TransmitBookmarkCollection();
        assertEquals(0, c.size());
        c.parse(LocalFactory.createLocal("test/ch/cyberduck/core/importer/com.panic.Transmit.plist"));
        assertEquals(1, c.size());
    }
}