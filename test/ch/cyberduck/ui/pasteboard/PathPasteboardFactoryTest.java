package ch.cyberduck.ui.pasteboard;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Protocol;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class PathPasteboardFactoryTest extends AbstractTestCase {

    @Test
    public void testGetPasteboard() throws Exception {
        final NullSession s = new NullSession(new Host(Protocol.SFTP, "l"));
        final PathPasteboard pasteboard = PathPasteboardFactory.getPasteboard(s);
        assertNotNull(pasteboard);
        assertEquals(pasteboard, PathPasteboardFactory.getPasteboard(s));
        assertSame(pasteboard, PathPasteboardFactory.getPasteboard(s));
    }
}
