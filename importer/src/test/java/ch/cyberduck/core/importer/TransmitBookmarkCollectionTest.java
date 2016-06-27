package ch.cyberduck.core.importer;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TransmitBookmarkCollectionTest {

    @Test(expected = AccessDeniedException.class)
    public void testParseNotFound() throws Exception {
        new TransmitBookmarkCollection().parse(new Local(System.getProperty("java.io.tmpdir"), "f"));
    }

    @Test
    @Ignore
    public void testParse() throws Exception {
        TransmitBookmarkCollection c = new TransmitBookmarkCollection();
        assertEquals(0, c.size());
        c.parse(new Local("src/test/resources/com.panic.Transmit.plist"));
        assertEquals(1, c.size());
    }
}