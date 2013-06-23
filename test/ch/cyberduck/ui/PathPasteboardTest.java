package ch.cyberduck.ui;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Protocol;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @version $Id:$
 */
public class PathPasteboardTest extends AbstractTestCase {

    @Test
    public void testGetPasteboard() throws Exception {
        assertNotNull(PathPasteboard.getPasteboard(new NullSession(new Host(Protocol.SFTP, "l"))));
    }
}
