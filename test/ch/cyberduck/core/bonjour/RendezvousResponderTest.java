package ch.cyberduck.core.bonjour;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.test.Depends;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class RendezvousResponderTest extends AbstractTestCase {

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
//                    try {
//                        assertEquals(String.format("%s.", InetAddress.getLocalHost().getHostName()), host.getHostname());
//                    }
//                    catch(UnknownHostException e) {
//                        fail();
//                    }
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

    @Test(expected = IllegalStateException.class)
    public void testShutdown() throws Exception {
        final RendezvousResponder r = new RendezvousResponder();
        r.init();
        r.quit();
        r.add("t-name", new Host("h"));
    }

    @Test
    public void testGetProtocol() throws Exception {
        final AbstractRendezvous r = new RendezvousResponder();
        Assert.assertEquals(ProtocolFactory.FTP, r.getProtocol("andaman._ftp._tcp.local."));
        assertEquals(ProtocolFactory.SFTP, r.getProtocol("yuksom._sftp-ssh._tcp."));
        assertEquals(ProtocolFactory.WEBDAV, r.getProtocol("yuksom._webdav._tcp"));
        assertEquals(ProtocolFactory.WEBDAV_SSL, r.getProtocol("andaman._webdavs._tcp"));
        assertNull(r.getProtocol("andaman._g._tcp"));
    }
}