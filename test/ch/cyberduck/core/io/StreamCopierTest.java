package ch.cyberduck.core.io;

import ch.cyberduck.core.AbstractTestCase;
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
    public void testTransferUnknownLength() throws Exception {
        final TransferStatus status = new TransferStatus();
        new StreamCopier(status, status).transfer(new NullInputStream(432768L), -1, new NullOutputStream(),
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
        assertEquals(432768L, status.getCurrent(), 0L);
    }

    @Test
    public void testTransferFixedLength() throws Exception {
        final TransferStatus status = new TransferStatus();
        new StreamCopier(status, status).transfer(new NullInputStream(432768L), -1, new NullOutputStream(),
                new DisabledStreamListener(), 432768L);
        assertTrue(status.isComplete());
        assertEquals(432768L, status.getCurrent(), 0L);
    }

    @Test
    public void testTransferFixedLengthIncomplete() throws Exception {
        final TransferStatus status = new TransferStatus();
        new StreamCopier(status, status).transfer(new NullInputStream(432768L), -1, new NullOutputStream(),
                new DisabledStreamListener(), 432767L);
        assertEquals(432767L, status.getCurrent(), 0L);
        assertTrue(status.isComplete());
    }

    @Test
    public void testSkipInput() throws Exception {
        {
            final TransferStatus status = new TransferStatus();
            new StreamCopier(status, status).transfer(new NullInputStream(432768L), 1, new NullOutputStream(),
                    new DisabledStreamListener(), 432768L);
            assertEquals(432767L, status.getCurrent(), 0L);
            assertTrue(status.isComplete());
        }
        {
            final TransferStatus status = new TransferStatus();
            new StreamCopier(status, status).transfer(new NullInputStream(432768L), 1, new NullOutputStream(),
                    new DisabledStreamListener(), -1);
            assertEquals(432767L, status.getCurrent(), 0L);
            assertTrue(status.isComplete());
        }
    }

    @Test
    public void testTransferInterrupt() throws Exception {
        this.repeat(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
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

                                @Override
                                public void setComplete() {

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
