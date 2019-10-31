package ch.cyberduck.core.importer;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.AccessDeniedException;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class FetchBookmarkCollectionTest {

    @Test(expected = AccessDeniedException.class)
    public void testParseNotFound() throws Exception {
        new FetchBookmarkCollection().parse(new ProtocolFactory(Collections.emptySet()), new Local(System.getProperty("java.io.tmpdir"), "f"));
    }

    @Test
    public void testGetFile() throws Exception {
        FetchBookmarkCollection c = new FetchBookmarkCollection();
        assertEquals(0, c.size());
        c.parse(new ProtocolFactory(new HashSet<>(Arrays.asList(new TestProtocol(Scheme.ftp), new TestProtocol(Scheme.ftps), new TestProtocol(Scheme.sftp)))), new Local("src/test/resources/com.fetchsoftworks.Fetch.Shortcuts.plist"));
        assertEquals(2, c.size());
    }
}
