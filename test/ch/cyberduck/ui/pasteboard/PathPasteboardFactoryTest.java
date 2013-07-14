package ch.cyberduck.ui.pasteboard;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.ftp.FTPSession;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class PathPasteboardFactoryTest extends AbstractTestCase {

    @Test
    public void testGetPasteboard() throws Exception {
        final Session s = new FTPSession(new Host(Protocol.SFTP, "l"));
        final PathPasteboard pasteboard = PathPasteboardFactory.getPasteboard(s);
        assertNotNull(pasteboard);
        assertEquals(pasteboard, PathPasteboardFactory.getPasteboard(s));
        assertSame(pasteboard, PathPasteboardFactory.getPasteboard(s));
    }
}
