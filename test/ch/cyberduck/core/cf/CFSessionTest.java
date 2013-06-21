package ch.cyberduck.core.cf;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class CFSessionTest extends AbstractTestCase {

    @Test
    public void testFile() {
        final CFSession session = new CFSession(new Host(Protocol.SWIFT, "h"));
        assertFalse(session.isCreateFileSupported(new CFPath(session, "/", Path.VOLUME_TYPE)));
        assertTrue(session.isCreateFileSupported(new CFPath(session, "/container", Path.VOLUME_TYPE)));
    }
}
