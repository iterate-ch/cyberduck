package ch.cyberduck.ui.cocoa;

import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.core.AbstractController;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.AbstractBackgroundAction;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.core.threading.MainAction;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class ProxyControllerTest {

    @Test
    public void testBackground() throws Exception {
        final AbstractController controller = new ProxyController();
        final CountDownLatch entry = new CountDownLatch(1);
        final CountDownLatch exit = new CountDownLatch(1);
        final AbstractBackgroundAction<Object> action = new AbstractBackgroundAction<Object>() {
            @Override
            public void init() {
                assertEquals("main", Thread.currentThread().getName());
            }

            @Override
            public Object run() throws BackgroundException {
                assertEquals("background-1", Thread.currentThread().getName());
                return null;
            }

            @Override
            public void cleanup() {
                assertEquals("main", Thread.currentThread().getName());
                assertFalse(controller.getRegistry().contains(this));
            }

        };
        controller.background(action).get();
    }

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
                    }
                }, true);
            }
        }.start();
        entry.await(1, TimeUnit.SECONDS);
        assertTrue(c.get());
    }
}
