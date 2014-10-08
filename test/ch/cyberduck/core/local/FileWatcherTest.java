package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Local;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @version $Id$
 */
@Ignore
public class FileWatcherTest extends AbstractTestCase {

    @Test
    public void testAddListener() throws Exception {
        final FileWatcher watcher = new FileWatcher();
//        final FinderLocal file = new FinderLocal(System.getProperty("java.io.tmpdir") + "/f", UUID.randomUUID().toString());
        final FinderLocal file = new FinderLocal(System.getProperty("java.io.tmpdir") + "/fé", UUID.randomUUID().toString());
        final CyclicBarrier create = new CyclicBarrier(2);
        final CyclicBarrier delete = new CyclicBarrier(2);
        final FileWatcherListener listener = new FileWatcherListener() {
            @Override
            public void fileWritten(final Local file) {
                try {
                    assertEquals(new File(file.getAbsolute()).getCanonicalPath(), new File(file.getAbsolute()).getCanonicalPath());
                }
                catch(IOException e) {
                    fail();
                }
            }

            @Override
            public void fileDeleted(final Local file) {
                try {
                    assertEquals(new File(file.getAbsolute()).getCanonicalPath(), new File(file.getAbsolute()).getCanonicalPath());
                }
                catch(IOException e) {
                    fail();
                }
                try {
                    delete.await(1L, TimeUnit.SECONDS);
                }
                catch(InterruptedException e) {
                    fail();
                }
                catch(BrokenBarrierException e) {
                    fail();
                }
                catch(TimeoutException e) {
                    fail();
                }
            }

            @Override
            public void fileCreated(final Local file) {
                try {
                    assertEquals(new File(file.getAbsolute()).getCanonicalPath(), new File(file.getAbsolute()).getCanonicalPath());
                }
                catch(IOException e) {
                    fail();
                }
                try {
                    create.await(1L, TimeUnit.SECONDS);
                }
                catch(InterruptedException e) {
                    fail();
                }
                catch(BrokenBarrierException e) {
                    fail();
                }
                catch(TimeoutException e) {
                    fail();
                }
            }
        };
        watcher.addListener(listener);
        watcher.register(file).await();
        LocalTouchFactory.get().touch(file);
        create.await();
        file.delete();
        delete.await();
        watcher.removeListener(listener);
        watcher.close(file);
    }
}