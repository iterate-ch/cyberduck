package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class SpeedometerTest extends AbstractTestCase {

    @Test
    public void testProgress() throws Exception {
        Transfer t = new DownloadTransfer(new NullPath("a", Path.FILE_TYPE)) {
            @Override
            public boolean isRunning() {
                return true;
            }
        };
        Speedometer m = new Speedometer(t);
        t.size = 5L;
        t.transferred = 0L;
        assertEquals("0 B of 5 B (0%)", m.getProgress());
        Thread.sleep(1000L);
        t.transferred = 1L;
        assertEquals("1 B of 5 B (20%, 1 B/sec, 5 seconds remaining)", m.getProgress());
        Thread.sleep(1000L);
        t.transferred = 4L;
        assertEquals("4 B of 5 B (80%, 3 B/sec, 1 seconds remaining)", m.getProgress());
    }

    @Test
    public void testProgressMb() throws Exception {
        Transfer t = new DownloadTransfer(new NullPath("a", Path.FILE_TYPE)) {
            @Override
            public boolean isRunning() {
                return true;
            }
        };
        Speedometer m = new Speedometer(t);
        t.size = 1000000;
        t.transferred = 0L;
        assertEquals("0 B of 1.0 MB (0%)", m.getProgress());
        Thread.sleep(1000L);
        t.transferred = 1000000 / 2;
        assertEquals("500.0 KB (500,000 bytes) of 1.0 MB (50%, 500.0 KB/sec, 2 seconds remaining)", m.getProgress());
        Thread.sleep(1000L);
        t.transferred = 900000;
        assertEquals("900.0 KB (900,000 bytes) of 1.0 MB (90%, 400.0 KB/sec, 1 seconds remaining)", m.getProgress());
    }
}