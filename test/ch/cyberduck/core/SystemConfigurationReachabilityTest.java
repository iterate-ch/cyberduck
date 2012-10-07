package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class SystemConfigurationReachabilityTest extends AbstractTestCase {

    @Test
    public void testIsReachable() throws Exception {
        assertTrue(ReachabilityFactory.instance().isReachable(
                new Host("cyberduck.ch", 80)
        ));
        assertTrue(ReachabilityFactory.instance().isReachable(
                new Host("cyberduck.ch", 22)
        ));
        assertFalse(ReachabilityFactory.instance().isReachable(
                new Host("a.cyberduck.ch", 22)
        ));
    }
}
