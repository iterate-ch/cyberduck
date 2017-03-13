package ch.cyberduck.core;

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.AbstractBackgroundAction;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.core.threading.MainAction;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class AbstractControllerTest {

    @Test
    public void testBackground() throws Exception {
        final AbstractController controller = new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                assertEquals("main", Thread.currentThread().getName());
            }
        };

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
                entry.countDown();
                try {
                    exit.await(1, TimeUnit.SECONDS);
                }
                catch(InterruptedException e) {
                    fail();
                }
                return null;
            }

            @Override
            public void cleanup() {
                assertEquals("main", Thread.currentThread().getName());
                assertFalse(controller.getRegistry().contains(this));
            }

        };
        controller.background(action);
        controller.background(new AbstractBackgroundAction<Object>() {
            @Override
            public Object run() throws BackgroundException {
                assertFalse(controller.getRegistry().contains(action));
                return null;
            }

        });
        entry.await(1, TimeUnit.SECONDS);
        assertTrue(controller.getRegistry().contains(action));
        exit.countDown();
    }

    @Test
    public void testInvoke() throws Exception {
        final CountDownLatch entry = new CountDownLatch(1);
        final AbstractController controller = new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                assertFalse(wait);
                entry.countDown();
            }
        };
        new Thread() {
            @Override
            public void run() {
                controller.invoke(new DefaultMainAction() {
                    @Override
                    public void run() {
                        //
                    }
                });
            }
        }.start();
        entry.await(1, TimeUnit.SECONDS);
    }
}