package ch.cyberduck.core;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
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
            public void serviceLost(final String servicename) {
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
}