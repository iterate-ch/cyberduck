package ch.cyberduck.core.importer;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.ftp.FTPProtocol;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FetchBookmarkCollectionTest {

    @Test(expected = AccessDeniedException.class)
    public void testParseNotFound() throws Exception {
        new FetchBookmarkCollection().parse(new Local(System.getProperty("java.io.tmpdir"), "f"));
    }

    @Test
    public void testGetFile() throws Exception {
        ProtocolFactory.register(new FTPProtocol());
        FetchBookmarkCollection c = new FetchBookmarkCollection();
        assertEquals(0, c.size());
        c.parse(new Local("src/test/resources/com.fetchsoftworks.Fetch.Shortcuts.plist"));
        assertEquals(2, c.size());
    }
}
