package ch.cyberduck.ui;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Protocol;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class PathPasteboardTest extends AbstractTestCase {

    @Test
    public void testGetPasteboard() throws Exception {
        final NullSession s = new NullSession(new Host(Protocol.SFTP, "l"));
        final PathPasteboard pasteboard = PathPasteboard.getPasteboard(s);
        assertNotNull(pasteboard);
        assertEquals(pasteboard, PathPasteboard.getPasteboard(s));
        assertSame(pasteboard, PathPasteboard.getPasteboard(s));
    }
}
