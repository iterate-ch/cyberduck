package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * @version $Id:$
 */
public class ProtocolFactoryTest extends AbstractTestCase {

    @Test
    public void testRegister() throws Exception {
        assertFalse(ProtocolFactory.getKnownProtocols().isEmpty());
    }
}