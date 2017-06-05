package ch.cyberduck.core.bonjour;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
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
    public void testGetProtocol() throws Exception {
        final AbstractRendezvous r = new RendezvousResponder(new ProtocolFactory(new HashSet<>(Arrays.asList(new TestProtocol(Scheme.sftp),
                new TestProtocol(Scheme.ftp), new TestProtocol(Scheme.dav), new TestProtocol(Scheme.davs)))));
        assertEquals(new TestProtocol(Scheme.ftp), r.getProtocol("andaman._ftp._tcp.local."));
        assertEquals(new TestProtocol(Scheme.sftp), r.getProtocol("yuksom._sftp-ssh._tcp."));
        assertEquals(new TestProtocol(Scheme.dav), r.getProtocol("yuksom._webdav._tcp"));
        assertEquals(new TestProtocol(Scheme.davs), r.getProtocol("andaman._webdavs._tcp"));
        assertNull(r.getProtocol("andaman._g._tcp"));
    }
}