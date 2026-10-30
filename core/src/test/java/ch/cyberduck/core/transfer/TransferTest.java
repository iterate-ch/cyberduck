package ch.cyberduck.core.transfer;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class TransferTest {

    @Test
    public void testSizeUnknown() {
        final Transfer t = new DownloadTransfer(new Host(new TestProtocol()), new Path("/t", EnumSet.of(Path.Type.file)), new NullLocal("l"));
        assertFalse(t.isComplete());
        assertEquals(0L, t.getSize().longValue());
        assertEquals(0L, t.getTransferred().longValue());
        t.addSize(0L);
        t.addTransferred(0L);
        assertTrue(t.isComplete());
        t.reset();
        assertFalse(t.isComplete());
        assertEquals(0L, t.getSize().longValue());
        assertEquals(0L, t.getTransferred().longValue());
    }

    /**
     * Files are prepared concurrently on a thread pool. Counters must not lose updates when
     * multiple threads add bytes right after the transfer has been reset.
     */
    @Test
    public void testConcurrentAddAfterReset() throws Exception {
        final Transfer t = new DownloadTransfer(new Host(new TestProtocol()), new Path("/t", EnumSet.of(Path.Type.file)), new NullLocal("l"));
        final int threads = Math.max(2, Runtime.getRuntime().availableProcessors());
        final int rounds = 100;
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            for(int round = 0; round < rounds; round++) {
                t.reset();
                // Spin instead of parking to release all threads within the initialization window
                final AtomicInteger ready = new AtomicInteger();
                final AtomicBoolean go = new AtomicBoolean();
                final List<Future<Void>> futures = new ArrayList<>();
                for(int i = 0; i < threads; i++) {
                    futures.add(pool.submit(new Callable<Void>() {
                        @Override
                        public Void call() {
                            ready.incrementAndGet();
                            while(!go.get()) {
                                // Busy wait
                            }
                            t.addSize(1L);
                            t.addTransferred(1L);
                            return null;
                        }
                    }));
                }
                while(ready.get() < threads) {
                    // Wait for all threads to spin
                }
                go.set(true);
                for(Future<Void> f : futures) {
                    f.get();
                }
                assertEquals(String.format("Lost update in round %d", round), threads, t.getSize().longValue());
                assertEquals(String.format("Lost update in round %d", round), threads, t.getTransferred().longValue());
                assertTrue(t.isComplete());
            }
        }
        finally {
            pool.shutdownNow();
        }
    }
}
