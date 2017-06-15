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

public class FlowBookmarkCollectionTest {

    @Test(expected = AccessDeniedException.class)
    public void testParseNotFound() throws Exception {
        new FlowBookmarkCollection().parse(new ProtocolFactory(Collections.emptySet()), new Local(System.getProperty("java.io.tmpdir"), "f"));
    }

    @Test
    public void testParse() throws AccessDeniedException {
        FlowBookmarkCollection c = new FlowBookmarkCollection();
        assertEquals(0, c.size());
        c.parse(new ProtocolFactory(new HashSet<>(Arrays.asList(new TestProtocol(Scheme.ftp), new TestProtocol(Scheme.ftps), new TestProtocol(Scheme.https)))), new Local("src/test/resources/com.fivedetails.Bookmarks.plist"));
        assertEquals(3, c.size());
    }
}