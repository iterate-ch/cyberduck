package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @version $Id:$
 */
public class FileWatcherTest extends AbstractTestCase {

    @Test
    public void testAddListener() throws Exception {
        final FileWatcher w = new FileWatcher();
        final FinderLocal f = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final CyclicBarrier create = new CyclicBarrier(2);
        final CyclicBarrier delete = new CyclicBarrier(2);
        w.addListener(new FileWatcherListener() {
            @Override
            public void fileWritten(final Local file) {
                assertEquals(f, file);
            }

            @Override
            public void fileRenamed(final Local file) {
                try {
                    assertEquals(new File(f.getAbsolute()).getCanonicalPath(), new File(file.getAbsolute()).getCanonicalPath());
                }
                catch(IOException e) {
                    fail();
                }
            }

            @Override
            public void fileDeleted(final Local file) {
                try {
                    assertEquals(new File(f.getAbsolute()).getCanonicalPath(), new File(file.getAbsolute()).getCanonicalPath());
                }
                catch(IOException e) {
                    fail();
                }
                try {
                    delete.await();
                }
                catch(InterruptedException e) {
                    fail();
                }
                catch(BrokenBarrierException e) {
                    fail();
                }
            }

            @Override
            public void fileCreated(final Local file) {
                try {
                    assertEquals(new File(f.getAbsolute()).getCanonicalPath(), new File(file.getAbsolute()).getCanonicalPath());
                }
                catch(IOException e) {
                    fail();
                }
                try {
                    create.await();
                }
                catch(InterruptedException e) {
                    fail();
                }
                catch(BrokenBarrierException e) {
                    fail();
                }
            }
        });
        w.register(f);
        f.touch();
        create.await();
        f.delete();
        delete.await();
    }
}