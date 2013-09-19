package ch.cyberduck.ui.cocoa;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.core.threading.MainAction;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class ProxyControllerTest extends AbstractTestCase {

    @Test
    public void testInvokeNoWait() throws Exception {
        this.repeat(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
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
                entry.await();
                invoked.await();
                assertTrue(c.get());
                return null;
            }
        }, 100);
    }

    @Test
    public void testInvokeWait() throws Exception {
        this.repeat(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
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
                entry.await();
                assertTrue(c.get());
                return null;
            }
        }, 100);
    }
}
