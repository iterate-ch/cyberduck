package ch.cyberduck.core;

import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class RendezvousResponderTest extends AbstractTestCase {

    @Before
    public void init() {
        RendezvousResponder.register();
    }

    @Test
    public void testInit() throws Exception {
        Rendezvous r = RendezvousFactory.instance();
        final CountDownLatch wait = new CountDownLatch(1);
        r.addListener(new RendezvousListener() {
            @Override
            public void serviceResolved(final String identifier, final Host host) {
                assertNotNull(host);
                try {
                    assertEquals(String.format("%s.", InetAddress.getLocalHost().getHostName()), host.getHostname());
                }
                catch(UnknownHostException e) {
                    fail();
                }
                finally {
                    wait.countDown();
                }
            }

            @Override
            public void serviceLost(final String servicename) {
                //
            }
        });
        r.init();
        wait.await();
        r.quit();
    }
}