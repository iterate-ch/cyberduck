package ch.cyberduck.core.io;

import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class StreamCopierTest {

    @Test
    public void testIntegrity() throws Exception {
        final byte[] bytes = RandomUtils.nextBytes(39865);
        final TransferStatus status = new TransferStatus();
        final ByteArrayOutputStream out = new ByteArrayOutputStream(bytes.length);
        final BytecountStreamListener count = new BytecountStreamListener();
        new StreamCopier(status, status).withLimit((long) bytes.length).withListener(count).transfer(new ByteArrayInputStream(bytes), out);
        assertEquals(bytes.length, count.getRecv());
        assertEquals(bytes.length, count.getSent());
        assertEquals(0L, status.getOffset());
        assertArrayEquals(bytes, out.toByteArray());
        assertTrue(status.isComplete());
    }

    @Test
    public void testTransferUnknownLength() throws Exception {
        final TransferStatus status = new TransferStatus();
        final BytecountStreamListener count = new BytecountStreamListener() {
            @Override
            public void sent(long bytes) {
                assertTrue(bytes > 0L);
                assertTrue(bytes <= 32768L);
                super.sent(bytes);
                assertEquals(this.getSent(), this.getRecv());
            }

            @Override
            public void recv(long bytes) {
                assertTrue(bytes > 0L);
                assertTrue(bytes <= 32768L);
                super.recv(bytes);
                assertTrue(this.getRecv() > this.getSent());
            }
        };
        new StreamCopier(status, status).withListener(count).transfer(new NullInputStream(432768L), NullOutputStream.NULL_OUTPUT_STREAM);
        assertTrue(status.isComplete());
        assertEquals(0L, status.getOffset());
        assertEquals(432768L, count.getSent());
        assertEquals(432768L, count.getRecv());
    }

    @Test
    public void testTransferIncorrectLength() throws Exception {
        final TransferStatus status = new TransferStatus();
        final AtomicBoolean write = new AtomicBoolean();
        final BytecountStreamListener count = new BytecountStreamListener() {
            @Override
            public void sent(final long bytes) {
                assertEquals(5L, bytes);
                super.sent(bytes);
            }

            @Override
            public void recv(final long bytes) {
                assertEquals(5L, bytes);
                super.recv(bytes);
            }
        };
        new StreamCopier(status, status).withLimit(10L).withListener(count).transfer(new NullInputStream(5L), new NullOutputStream() {
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
        assertEquals(5L, count.getRecv());
        assertEquals(5L, count.getSent());
        assertEquals(0L, status.getOffset());
    }

    @Test
    public void testTransferFixedLength() throws Exception {
        final TransferStatus status = new TransferStatus().withLength(432768L);
        final BytecountStreamListener count = new BytecountStreamListener();
        new StreamCopier(status, status).withLimit(432767L).withListener(count).transfer(new NullInputStream(432768L), NullOutputStream.NULL_OUTPUT_STREAM);
        assertEquals(0L, status.getOffset());
        assertEquals(432767L, count.getSent());
        assertEquals(432767L, count.getRecv());
        assertTrue(status.isComplete());
    }

    @Test
    public void testReadNoEndofStream() throws Exception {
        final TransferStatus status = new TransferStatus().withLength(432768L);
        final BytecountStreamListener count = new BytecountStreamListener();
        new StreamCopier(status, status).withLimit(432768L).withListener(count).transfer(new NullInputStream(432770L), NullOutputStream.NULL_OUTPUT_STREAM);
        assertEquals(432768L, count.getRecv());
        assertEquals(432768L, count.getSent());
        assertEquals(0L, status.getOffset());
        assertTrue(status.isComplete());
    }

    @Test
    public void testSkipInput() throws Exception {
        {
            final TransferStatus status = new TransferStatus();
            final BytecountStreamListener count = new BytecountStreamListener();
            new StreamCopier(status, status).withOffset(1L).withListener(count).transfer(new NullInputStream(432768L), NullOutputStream.NULL_OUTPUT_STREAM);
            assertEquals(432767L, count.getSent());
            assertEquals(432767L, count.getRecv());
            assertEquals(0L, status.getOffset());
            assertTrue(status.isComplete());
        }
        {
            final TransferStatus status = new TransferStatus();
            final BytecountStreamListener count = new BytecountStreamListener();
            new StreamCopier(status, status).withOffset(1L).withListener(count).transfer(new NullInputStream(432768L), NullOutputStream.NULL_OUTPUT_STREAM);
            assertEquals(432767L, count.getSent());
            assertEquals(432767L, count.getRecv());
            assertEquals(0L, status.getOffset());
            assertTrue(status.isComplete());
        }
    }

    @Test
    public void testTransferInterrupt() throws Exception {
        final TransferStatus status = new TransferStatus();
        final CyclicBarrier lock = new CyclicBarrier(2);
        final CyclicBarrier exit = new CyclicBarrier(2);
        status.setLength(432768L);
        final BytecountStreamListener count = new BytecountStreamListener() {
            @Override
            public void sent(final long bytes) {
                super.sent(bytes);
                try {
                    lock.await();
                    exit.await();
                }
                catch(InterruptedException | BrokenBarrierException e) {
                    fail(e.getMessage());
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new StreamCopier(status, status).withListener(count).transfer(new NullInputStream(status.getLength()), NullOutputStream.NULL_OUTPUT_STREAM);
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
        try {
            status.validate();
            fail();
        }
        catch(ConnectionCanceledException e) {

        }
        assertEquals(32768L, count.getRecv());
        assertEquals(32768L, count.getSent());
        assertEquals(0L, status.getOffset());
    }
}
