package ch.cyberduck.core.io;

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class StreamCopierTest {

    @Test
    public void testIntegrity() throws Exception {
        final String random = RandomStringUtils.random(39865);
        final byte[] bytes = random.getBytes();
        final TransferStatus status = new TransferStatus();
        final ByteArrayOutputStream out = new ByteArrayOutputStream(bytes.length);
        new StreamCopier(status, status).withLimit((long) bytes.length).transfer(IOUtils.toInputStream(random, Charset.defaultCharset()), out);
        assertEquals(bytes.length, status.getOffset(), 0L);
        assertArrayEquals(bytes, out.toByteArray());
        assertTrue(status.isComplete());
    }

    @Test
    public void testTransferUnknownLength() throws Exception {
        final TransferStatus status = new TransferStatus();
        new StreamCopier(status, status).withListener(new StreamListener() {
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
        }).transfer(new NullInputStream(432768L), new NullOutputStream());
        assertTrue(status.isComplete());
        assertEquals(432768L, status.getOffset(), 0L);
    }

    @Test
    public void testTransferIncorrectLength() throws Exception {
        final TransferStatus status = new TransferStatus();
        final AtomicBoolean write = new AtomicBoolean();
        new StreamCopier(status, status).withLimit(10L).withListener(new DisabledStreamListener() {
            @Override
            public void sent(final long bytes) {
                assertEquals(5L, bytes);
            }

            @Override
            public void recv(final long bytes) {
                assertEquals(5L, bytes);
            }
        }).transfer(new NullInputStream(5L), new NullOutputStream() {
                    @Override
                    public void write(final byte[] b, final int off, final int len) {
                        assertEquals(0, off);
                        assertEquals(5, len);
                        write.set(true);
                    }
                }
        );
        assertTrue(write.get());
        assertTrue(status.isComplete());
        assertEquals(5L, status.getOffset(), 0L);
    }

    @Test
    public void testTransferFixedLength() throws Exception {
        final TransferStatus status = new TransferStatus().length(432768L);
        new StreamCopier(status, status).withLimit(432768L).transfer(new NullInputStream(432768L), new NullOutputStream());
        assertTrue(status.isComplete());
        assertEquals(432768L, status.getOffset(), 0L);
    }

    @Test
    public void testTransferFixedLengthIncomplete() throws Exception {
        final TransferStatus status = new TransferStatus().length(432768L);
        new StreamCopier(status, status).withLimit(432767L).transfer(new NullInputStream(432768L), new NullOutputStream());
        assertEquals(432767L, status.getOffset(), 0L);
        assertTrue(status.isComplete());
    }

    @Test
    public void testReadNoEndofStream() throws Exception {
        final TransferStatus status = new TransferStatus().length(432768L);
        new StreamCopier(status, status).withLimit(432768L).transfer(new NullInputStream(432770L), new NullOutputStream());
        assertEquals(432768L, status.getOffset(), 0L);
        assertTrue(status.isComplete());
    }

    @Test
    public void testSkipInput() throws Exception {
        {
            final TransferStatus status = new TransferStatus();
            new StreamCopier(status, status).withOffset(1L).transfer(new NullInputStream(432768L), new NullOutputStream());
            assertEquals(432767L, status.getOffset(), 0L);
            assertTrue(status.isComplete());
        }
        {
            final TransferStatus status = new TransferStatus();
            new StreamCopier(status, status).withOffset(1L).transfer(new NullInputStream(432768L), new NullOutputStream());
            assertEquals(432767L, status.getOffset(), 0L);
            assertTrue(status.isComplete());
        }
    }

    @Test
    public void testTransferInterrupt() throws Exception {
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
                            catch(InterruptedException | BrokenBarrierException e) {
                                fail(e.getMessage());
                            }
                        }

                        @Override
                        public void setComplete() {

                        }
                    }).transfer(new NullInputStream(status.getLength()), new NullOutputStream());
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
        assertEquals(32768L, status.getOffset());
    }
}
