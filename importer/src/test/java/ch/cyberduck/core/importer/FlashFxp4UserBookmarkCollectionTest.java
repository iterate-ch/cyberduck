package ch.cyberduck.core.importer;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.AccessDeniedException;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class FlashFxp4UserBookmarkCollectionTest {

    @Test(expected = AccessDeniedException.class)
    public void testParseNotFound() throws Exception {
        new FlashFxp4UserBookmarkCollection().parse(new ProtocolFactory(Collections.emptySet()), new Local(System.getProperty("java.io.tmpdir"), "f"));
    }

    @Test
    public void testParse() throws Exception {
        FlashFxpBookmarkCollection c = new FlashFxp4UserBookmarkCollection();
        assertEquals(0, c.size());
        c.parse(new ProtocolFactory(Collections.singleton(new TestProtocol(Scheme.ftp))), new Local("src/test/resources/FlashFXP-Sites.dat"));
        assertEquals(4, c.size());
        assertEquals("ftp.intel.com", c.get(1).getHostname());
        assertEquals("ftp.sierra.com", c.get(2).getHostname());
    }
}