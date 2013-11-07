package ch.cyberduck.core.io;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class StreamCopierTest extends AbstractTestCase {

    @Test
    public void testTransfer() throws Exception {
        Path p = new Path("/t", Path.FILE_TYPE);
        final TransferStatus status = new TransferStatus();
        status.setLength(432768L);
        new StreamCopier(status, status).transfer(new NullInputStream(status.getLength()), -1, new NullOutputStream(),
                new StreamListener() {
                    long sent;
                    long received;

                    @Override
                    public void sent(long bytes) {
                        assertTrue(bytes > 0L);
                        assertTrue(bytes <= 32768L);
                        sent += bytes;
                        assertTrue(sent == received);
                    }

                    @Override
                    public void recv(long bytes) {
                        assertTrue(bytes > 0L);
                        assertTrue(bytes <= 32768L);
                        received += bytes;
                        assertTrue(received > sent);
                    }
                }, -1);
        assertTrue(status.isComplete());
        assertTrue(status.getCurrent() == status.getLength());
    }

    @Test
    public void testTransferInterrupt() throws Exception {
        this.repeat(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final Path p = new Path("/t", Path.FILE_TYPE);
                final TransferStatus status = new TransferStatus();
                final CyclicBarrier lock = new CyclicBarrier(2);
                final CyclicBarrier exit = new CyclicBarrier(2);
                status.setLength(432768L);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new StreamCopier(status, new StreamProgress() {
                                @Override
                                public void progress(final long bytes) {
                                    status.progress(bytes);
                                    try {
                                        lock.await();
                                        exit.await();
                                    }
                                    catch(InterruptedException e) {
                                        fail(e.getMessage());
                                    }
                                    catch(BrokenBarrierException e) {
                                        fail(e.getMessage());
                                    }
                                }
                            }).transfer(new NullInputStream(status.getLength()), -1, new NullOutputStream(),
                                    new DisabledStreamListener(), -1);
                        }
                        catch(IOException e) {
                            fail();
                        }
                        catch(BackgroundException e) {
                            assertTrue(e instanceof ConnectionCanceledException);
                        }
                    }
                }).start();
                lock.await();
                status.setCanceled();
                exit.await();
                assertFalse(status.isComplete());
                assertTrue(status.isCanceled());
                assertEquals(32768L, status.getCurrent());
                return null;
            }
        }, 10);
    }
}
