package ch.cyberduck.ui.cocoa;

import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.AbstractBackgroundAction;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.core.threading.MainAction;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class ProxyControllerTest {

    @Test
    public void testInvokeNoWait() throws Exception {
        final CountDownLatch entry = new CountDownLatch(1);
        final CountDownLatch invoked = new CountDownLatch(1);
        final AtomicBoolean c = new AtomicBoolean();
        final ProxyController controller = new ProxyController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                assertFalse(wait);
                super.invoke(runnable, wait);
                assertFalse(c.get());
                entry.countDown();
            }
        };
        new Thread() {
            @Override
            public void run() {
                controller.invoke(new DefaultMainAction() {
                    @Override
                    public void run() {
                        c.set(true);
                        invoked.countDown();
//                        assertEquals("main", Thread.currentThread().getName());
                    }
                }, false);
            }
        }.start();
        entry.await(1, TimeUnit.SECONDS);
        invoked.await(1, TimeUnit.SECONDS);
        assertTrue(c.get());
    }

    @Test
    public void testInvokeWait() throws Exception {
        final CountDownLatch entry = new CountDownLatch(1);
        final AtomicBoolean c = new AtomicBoolean();
        final ProxyController controller = new ProxyController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                assertTrue(wait);
                super.invoke(runnable, wait);
                entry.countDown();
            }
        };
        new Thread() {
            @Override
            public void run() {
                controller.invoke(new DefaultMainAction() {
                    @Override
                    public void run() {
                        c.set(true);
//                        assertEquals("main", Thread.currentThread().getName());
                    }
                }, true);
            }
        }.start();
        entry.await(1, TimeUnit.SECONDS);
        assertTrue(c.get());
    }

    @Test
    public void testBackgroundTaskConcurrentCleanup() throws Exception {
        final ProxyController controller = new ProxyController();
        final Object session = new Object();

        final CountDownLatch connectLatch = new CountDownLatch(1);
        final AtomicBoolean connected = new AtomicBoolean();
        final AtomicInteger increment = new AtomicInteger(0);

        final CountDownLatch mounted = new CountDownLatch(1);
        // Connect
        controller.background(new AbstractBackgroundAction() {
            @Override
            public Object run() throws BackgroundException {
                try {
                    connectLatch.await(1, TimeUnit.MINUTES);
                }
                catch(InterruptedException e) {
                    fail();
                }
                connected.set(true);
                return null;
            }

            @Override
            public Object lock() {
                assertNotNull(session);
                return session;
            }

            @Override
            public void cleanup() {
                assertEquals(1, increment.incrementAndGet());
            }
        });
        // Disconnect before connect was successful
        controller.background(new AbstractBackgroundAction() {
            @Override
            public Object run() throws BackgroundException {
                assertTrue(connected.get());
                return null;
            }

            @Override
            public Object lock() {
                assertNotNull(session);
                return session;
            }

            @Override
            public void cleanup() {
                assertEquals(2, increment.incrementAndGet());
                // Initialize new session in cleanup from disconnect task as in browser controller
                // Not synchronized with first session
                final Object session2 = new Object();
                controller.background(new AbstractBackgroundAction() {
                    @Override
                    public Object run() throws BackgroundException {
                        assertTrue(connected.get());
                        return null;
                    }

                    @Override
                    public Object lock() {
                        assertNotNull(session2);
                        return session2;
                    }

                    @Override
                    public void cleanup() {
                        assertEquals(3, increment.incrementAndGet());
                        mounted.countDown();
                    }
                });
            }
        });
        connectLatch.countDown();
        mounted.await(1, TimeUnit.MINUTES);
    }
}
