package ch.cyberduck.core;

import ch.cyberduck.core.test.Depends;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class SystemConfigurationReachabilityTest extends AbstractTestCase {

    @Test
    public void testIsReachablePort80() throws Exception {
        final Reachability r = new SystemConfigurationReachability();
        assertTrue(r.isReachable(
                new Host("cyberduck.ch", 80)
        ));
    }

    @Test
    public void testIsReachablePort22() throws Exception {
        final Reachability r = new SystemConfigurationReachability();
        assertTrue(r.isReachable(
                new Host("cyberduck.ch", 22)
        ));
    }

    @Test
    public void testNotReachablePort22() throws Exception {
        final Reachability r = new SystemConfigurationReachability();
        assertFalse(r.isReachable(
                new Host("a.cyberduck.ch", 22)
        ));
    }
}
