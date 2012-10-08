package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @version $Id:$
 */
public class PathFactoryTest extends AbstractTestCase {

    @Test
    public void testCreatePath() throws Exception {
        for(Protocol p : ProtocolFactory.getKnownProtocols()) {
            assertNotNull(PathFactory.createPath(SessionFactory.createSession(new Host(Protocol.WEBDAV, "h")), "p", Path.FILE_TYPE));
        }
    }
}
