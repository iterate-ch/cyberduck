package ch.cyberduck.core.date;

import org.junit.Test;

import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class MDTMSecondsDateFormatterTest {

    @Test
    public void testParse() throws Exception {
        // Thursday, December 1, 1994 10:00:00 PM GMT
        final MDTMSecondsDateFormatter formatter = new MDTMSecondsDateFormatter();
        assertEquals(786319200000L, formatter.parse("19941201220000").getTime(), 0L);
        assertEquals("19941201220000", formatter.format(786319200000L, TimeZone.getTimeZone("UTC")));
    }

    @Test
    public void testPrint() {
        assertEquals("19941201170000", new MDTMSecondsDateFormatter().format(786297600000L, TimeZone.getTimeZone("Europe/Zurich")));
    }
}
