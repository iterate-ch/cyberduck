package ch.cyberduck.core.date;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class MDTMSecondsDateFormatterTest {

    @Test
    public void testParse() throws Exception {
        assertEquals(7.862976E11, new MDTMSecondsDateFormatter().parse("19941201170000").getTime(), 0L);
    }

    @Test
    public void testPrint() throws Exception {
        assertEquals("19941201170000", new MDTMSecondsDateFormatter().format((long) 7.862976E11));
    }
}
