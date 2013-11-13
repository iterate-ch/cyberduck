package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @version $Id$
 */
public class SessionFactoryTest extends AbstractTestCase {

    @Test
    public void testCreateSession() throws Exception {
        for(Protocol protocol : ProtocolFactory.getKnownProtocols()) {
            assertNotNull(SessionFactory.create(new Host(protocol, "h")));
        }
    }
}
