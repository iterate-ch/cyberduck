package ch.cyberduck.core.diagnostics;

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TestProtocol;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

@Ignore
public class SystemConfigurationReachabilityTest {

    @Test
    public void testMonitor() {
        final Reachability r = new SystemConfigurationReachability();
        final Reachability.Monitor monitor = r.monitor(new Host(new TestProtocol(), "cyberduck.ch", 80),
                new Reachability.Callback() {
                    @Override
                    public void change() {
                    }
                }
        ).start();
        assertSame(monitor, monitor.stop());
    }

    @Test
    public void testIsReachable() {
        final Reachability r = new SystemConfigurationReachability();
        assertTrue(r.isReachable(
                new Host(new TestProtocol(), "cyberduck.ch")));
    }

    @Test
    @Ignore
    public void testNotReachablePortSubdomain() {
        final Reachability r = new SystemConfigurationReachability();
        assertFalse(r.isReachable(
                new Host(new TestProtocol(), "a.cyberduck.ch")
        ));
    }

    @Test
    @Ignore
    public void testNotReachableWrongDomain() {
        final Reachability r = new SystemConfigurationReachability();
        assertFalse(r.isReachable(
                new Host(new TestProtocol(), "cyberduck.f")
        ));
    }

    @Test
    public void testReachabilityLocalDisk() {
        final Reachability r = new SystemConfigurationReachability();
        assertTrue(r.isReachable(
                new Host(new AbstractProtocol() {
                    @Override
                    public String getIdentifier() {
                        return Scheme.file.name();
                    }

                    @Override
                    public String getDescription() {
                        return Scheme.file.toString();
                    }

                    @Override
                    public Scheme getScheme() {
                        return Scheme.file;
                    }
                }, "cyberduck.ch", 23)
        ));
    }
}
