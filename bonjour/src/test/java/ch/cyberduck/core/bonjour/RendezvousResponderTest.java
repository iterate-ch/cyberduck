package ch.cyberduck.core.bonjour;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.sftp.SFTPProtocol;

import org.junit.Assert;
import org.junit.Test;

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
        final AbstractRendezvous r = new RendezvousResponder();
        Assert.assertEquals(new FTPProtocol(), r.getProtocol("andaman._ftp._tcp.local."));
        assertEquals(new SFTPProtocol(), r.getProtocol("yuksom._sftp-ssh._tcp."));
        assertEquals(new DAVProtocol(), r.getProtocol("yuksom._webdav._tcp"));
        assertEquals(new DAVSSLProtocol(), r.getProtocol("andaman._webdavs._tcp"));
        assertNull(r.getProtocol("andaman._g._tcp"));
    }
}