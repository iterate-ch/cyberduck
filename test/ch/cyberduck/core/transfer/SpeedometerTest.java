package ch.cyberduck.core.transfer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NSObjectPathReference;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
@Ignore
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
        assertEquals("4 B of 5 B (80%, 2 B/sec, 1 seconds remaining)", m.getProgress(true, 5L, 4L));
    }

    @Test
    public void testProgressMb() throws Exception {
        Speedometer m = new Speedometer();
        assertEquals("0 B of 1.0 MB (0%)", m.getProgress(true, 1000000L, 0L));
        Thread.sleep(1000L);
        final String progress = m.getProgress(true, 1000000L, 1000000L / 2);
        assertTrue(progress,
                progress.matches("500.0 KB \\(500,000 bytes\\) of 1.0 MB \\(50%, \\d{3}.\\d KB/sec, 2 seconds remaining\\)"));
        Thread.sleep(1000L);
        final String progress1 = m.getProgress(true, 1000000L, 900000L);
        assertTrue(progress1,
                progress1.matches("900.0 KB \\(900,000 bytes\\) of 1.0 MB \\(90%, \\d{3}.\\d KB/sec, 1 seconds remaining\\)"));
    }
}