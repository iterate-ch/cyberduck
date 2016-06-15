package ch.cyberduck.core.date;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RemainingPeriodFormatterTest {

    @Test
    public void testFormat() throws Exception {
        RemainingPeriodFormatter f = new RemainingPeriodFormatter();
        assertEquals("5 seconds remaining", f.format(5L));
        assertEquals("5 minutes remaining", f.format(5 * 60));
        assertEquals("60 minutes remaining", f.format(60 * 60));
        assertEquals("120 minutes remaining", f.format(60 * 60 * 2));
        assertEquals("2.0 hours remaining", f.format(60 * 60 * 2 + 1));
        assertEquals("2.1 hours remaining", f.format(60 * 60 * 2 + 6 * 60));
    }
}