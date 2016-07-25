package ch.cyberduck.core.date;

import org.junit.Test;

import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class RFC1123DateFormatterTest {

    @Test
    public void testNull() throws Exception {
        assertEquals(null, new RFC1123DateFormatter().parse(null));
    }

    @Test
    public void testParse() throws Exception {
        assertEquals(786297600000L, new RFC1123DateFormatter().parse("Thu, 01 Dec 1994 16:00:00 GMT").getTime(), 0L);
    }

    @Test
    public void testPrint() throws Exception {
        assertEquals("Thu, 01 Dec 1994 17:00:00 CET", new RFC1123DateFormatter().format((long) 786297600000L, TimeZone.getTimeZone("Europe/Zurich")));
        assertEquals("Thu, 01 Dec 1994 16:00:00 UTC", new RFC1123DateFormatter().format((long) 786297600000L, TimeZone.getTimeZone("UTC")));
    }
}