package ch.cyberduck.core.date;

import org.junit.Test;

import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class MDTMSecondsDateFormatterTest {

    @Test
    public void testParse() throws Exception {
        // Thursday, December 1, 1994 10:00:00 PM GMT
        assertEquals(786319200000L, new MDTMSecondsDateFormatter().parse("19941201220000").getTime(), 0L);
    }

    @Test
    public void testPrint() throws Exception {
        assertEquals("19941201170000", new MDTMSecondsDateFormatter().format(786297600000L, TimeZone.getTimeZone("Europe/Zurich")));
    }
}
