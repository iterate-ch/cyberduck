package ch.cyberduck.core.diagnostics;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.TestProtocol;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SystemConfigurationReachabilityTest {

    @Test
    public void testIsReachablePort80() throws Exception {
        final Reachability r = new SystemConfigurationReachability();
        assertTrue(r.isReachable(
                new Host(new TestProtocol(), "cyberduck.ch", 80)
        ));
    }

    @Test
    public void testIsReachablePort22() throws Exception {
        final Reachability r = new SystemConfigurationReachability();
        assertTrue(r.isReachable(
                new Host(new TestProtocol(), "cyberduck.ch", 22)
        ));
    }

    @Test
    public void testNotReachablePortSubdomain() throws Exception {
        final Reachability r = new SystemConfigurationReachability();
        assertFalse(r.isReachable(
                new Host(new TestProtocol(), "a.cyberduck.ch", 22)
        ));
    }

    @Test
    public void testNotReachableWrongHostname() throws Exception {
        final Reachability r = new SystemConfigurationReachability();
        assertFalse(r.isReachable(
                new Host(new TestProtocol(), "cyberduck.ch.f", 80)
        ));
    }

    @Test
    @Ignore
    public void testNotReachableWrongPort() throws Exception {
        final Reachability r = new SystemConfigurationReachability();
        assertFalse(r.isReachable(
                new Host(new TestProtocol(), "cyberduck.ch", 23)
        ));
    }
}
