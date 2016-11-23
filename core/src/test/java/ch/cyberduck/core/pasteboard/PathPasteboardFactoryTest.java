package ch.cyberduck.core.pasteboard;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import static org.junit.Assert.*;

public class PathPasteboardFactoryTest {

    @Test
    public void testGetPasteboard() throws Exception {
        final Host s = new Host(new TestProtocol(Scheme.ftp), "l");
        final PathPasteboard pasteboard = PathPasteboardFactory.getPasteboard(s);
        assertNotNull(pasteboard);
        assertEquals(pasteboard, PathPasteboardFactory.getPasteboard(s));
        assertSame(pasteboard, PathPasteboardFactory.getPasteboard(s));
    }
}
