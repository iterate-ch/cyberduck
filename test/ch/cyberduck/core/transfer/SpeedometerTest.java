package ch.cyberduck.core.transfer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NSObjectPathReference;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class SpeedometerTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        NSObjectPathReference.register();
    }

    @Test
    public void testProgress() throws Exception {
        Speedometer m = new Speedometer();
        assertEquals("0 B of 5 B (0%)", m.getProgress(true, 5L, 0L));
        Thread.sleep(1000L);
        assertEquals("1 B of 5 B (20%, 1 B/sec, 5 seconds remaining)", m.getProgress(true, 5L, 1L));
        Thread.sleep(1000L);
        assertEquals("4 B of 5 B (80%, 3 B/sec, 1 seconds remaining)", m.getProgress(true, 5L, 4L));
    }

    @Test
    public void testProgressMb() throws Exception {
        Speedometer m = new Speedometer();
        assertEquals("0 B of 1.0 MB (0%)", m.getProgress(true, 1000000L, 0L));
        Thread.sleep(1000L);
        assertEquals("500.0 KB (500,000 bytes) of 1.0 MB (50%, 500.0 KB/sec, 2 seconds remaining)",
                m.getProgress(true, 1000000L, 1000000L / 2));
        Thread.sleep(1000L);
        assertEquals("900.0 KB (900,000 bytes) of 1.0 MB (90%, 400.0 KB/sec, 1 seconds remaining)",
                m.getProgress(true, 1000000L, 900000L));
    }
}