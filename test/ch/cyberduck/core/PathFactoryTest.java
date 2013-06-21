package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @version $Id$
 */
public class PathFactoryTest extends AbstractTestCase {

    @Test
    public void testCreatePath() throws Exception {
        for(Protocol p : ProtocolFactory.getKnownProtocols()) {
            final Path path = PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.WEBDAV, "h")), "p", Path.FILE_TYPE);
            assertNotNull(path);
            assertEquals("/p", path.getAbsolute());
            assertEquals("/", path.getParent().getAbsolute());
        }
    }
}
