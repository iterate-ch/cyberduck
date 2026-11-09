package ch.cyberduck.core.bonjour;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class RendezvousResponderTest {

    @Test
    public void testInit() throws Exception {
        final Rendezvous r = new RendezvousResponder();
        final CountDownLatch wait = new CountDownLatch(1);
        final AssertionError[] failure = new AssertionError[1];
        r.addListener(new RendezvousListener() {
            @Override
            public void serviceResolved(final String identifier, final Host host) {
                try {
                    assertNotNull(host);
                }
                catch(AssertionError error) {
                    failure[0] = error;
                }
                finally {
                    wait.countDown();
                }
            }

            @Override
            public void serviceLost(final Host servicename) {
                //
            }
        });
        r.init();
        wait.await(5L, TimeUnit.SECONDS);
        if(failure[0] != null) {
            fail(failure[0].getMessage());
        }
        r.quit();
    }

    @Test
    public void testServiceLostStopsPendingResolve() throws Exception {
        final RendezvousResponder r = new RendezvousResponder();
        final int baseline = resolverThreads();
        // Service that never resolves
        final String name = String.format("cyberduck-%s", UUID.randomUUID());
        r.serviceFound(null, 0, 0, name, "_sftp-ssh._tcp.", "local.");
        assertEquals(baseline + 1, awaitResolverThreads(baseline + 1));
        r.serviceLost(null, 0, 0, name, "_sftp-ssh._tcp.", "local.");
        assertEquals(baseline, awaitResolverThreads(baseline));
        r.quit();
    }

    @Test
    public void testServiceFoundTwiceResolvesOnce() throws Exception {
        final RendezvousResponder r = new RendezvousResponder();
        final int baseline = resolverThreads();
        final String name = String.format("cyberduck-%s", UUID.randomUUID());
        // Same service reported on multiple interfaces or again after flapping
        r.serviceFound(null, 0, 0, name, "_sftp-ssh._tcp.", "local.");
        r.serviceFound(null, 0, 0, name, "_sftp-ssh._tcp.", "local.");
        // Give a second resolver thread time to show up
        Thread.sleep(500L);
        assertEquals(baseline + 1, resolverThreads());
        r.quit();
        assertEquals(baseline, awaitResolverThreads(baseline));
    }

    @Test
    public void testServiceFoundOnMultipleInterfaces() throws Exception {
        final RendezvousResponder r = new RendezvousResponder();
        final int baseline = resolverThreads();
        final String name = String.format("cyberduck-%s", UUID.randomUUID());
        final int loopback = NetworkInterface.getByName("lo0").getIndex();
        // Same service reported on two interfaces is resolved on each
        r.serviceFound(null, 0, 0, name, "_sftp-ssh._tcp.", "local.");
        r.serviceFound(null, 0, loopback, name, "_sftp-ssh._tcp.", "local.");
        assertEquals(baseline + 2, awaitResolverThreads(baseline + 2));
        // Lost on one interface only stops resolve on this interface
        r.serviceLost(null, 0, loopback, name, "_sftp-ssh._tcp.", "local.");
        assertEquals(baseline + 1, awaitResolverThreads(baseline + 1));
        r.quit();
        assertEquals(baseline, awaitResolverThreads(baseline));
    }

    @Test
    public void testQuitStopsPendingResolve() throws Exception {
        final RendezvousResponder r = new RendezvousResponder();
        final int baseline = resolverThreads();
        r.serviceFound(null, 0, 0, String.format("cyberduck-%s", UUID.randomUUID()), "_sftp-ssh._tcp.", "local.");
        assertEquals(baseline + 1, awaitResolverThreads(baseline + 1));
        r.quit();
        assertEquals(baseline, awaitResolverThreads(baseline));
    }

    @Test
    public void testResolveTimeout() throws Exception {
        final RendezvousResponder r = new RendezvousResponder(ProtocolFactory.get(), 500L);
        final int baseline = resolverThreads();
        r.serviceFound(null, 0, 0, String.format("cyberduck-%s", UUID.randomUUID()), "_sftp-ssh._tcp.", "local.");
        assertEquals(baseline + 1, awaitResolverThreads(baseline + 1));
        // No call to serviceLost or quit
        assertEquals(baseline, awaitResolverThreads(baseline));
        r.quit();
    }

    /**
     * @return Number of threads polling a DNSSD service operation
     */
    private static int resolverThreads() {
        int count = 0;
        for(Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            for(StackTraceElement frame : entry.getValue()) {
                if("com.apple.dnssd.AppleService".equals(frame.getClassName())) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private static int awaitResolverThreads(final int expected) throws InterruptedException {
        int count = resolverThreads();
        for(int i = 0; i < 20 && count != expected; i++) {
            Thread.sleep(100L);
            count = resolverThreads();
        }
        return count;
    }

    @Test
    public void testGetProtocol() {
        final AbstractRendezvous r = new RendezvousResponder(new ProtocolFactory(new HashSet<>(Arrays.asList(new TestProtocol(Scheme.sftp),
                new TestProtocol(Scheme.ftp), new TestProtocol(Scheme.dav), new TestProtocol(Scheme.davs)))));
        assertEquals(new TestProtocol(Scheme.ftp), r.getProtocol("andaman._ftp._tcp.local."));
        assertEquals(new TestProtocol(Scheme.sftp), r.getProtocol("yuksom._sftp-ssh._tcp."));
        assertEquals(new TestProtocol(Scheme.dav), r.getProtocol("yuksom._webdav._tcp"));
        assertEquals(new TestProtocol(Scheme.davs), r.getProtocol("andaman._webdavs._tcp"));
        assertNull(r.getProtocol("andaman._g._tcp"));
    }
}