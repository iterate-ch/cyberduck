package ch.cyberduck.core;

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.AbstractBackgroundAction;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.core.threading.MainAction;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AbstractControllerTest {

    @Test
    public void testBackground() throws Exception {
        final AbstractController controller = new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                runnable.run();
            }
        };
        final AbstractBackgroundAction<Object> action = new AbstractBackgroundAction<Object>() {
            @Override
            public void init() {
                assertEquals("main", Thread.currentThread().getName());
            }

            @Override
            public Object run() throws BackgroundException {
                return null;
            }

            @Override
            public void cleanup() {
                assertFalse(controller.getRegistry().contains(this));
            }

        };
        controller.background(action).get();
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