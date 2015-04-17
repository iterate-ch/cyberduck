package ch.cyberduck.core.diagnostics;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.test.Depends;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
@Ignore
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
