package ch.cyberduck.core.formatter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DecimalSizeFormatterTest {

    @Test
    public void testFormat() {
        DecimalSizeFormatter f = new DecimalSizeFormatter();
        assertEquals("1.0 KB", f.format(1024L));
        assertEquals("1.5 KB", f.format(1500L));
        assertEquals("2.0 KB", f.format(2000L));
        assertEquals("1.0 MB", f.format(1048576L));
        assertEquals("1.1 GB", f.format(1073741824L));
        assertEquals("393.5 MB", f.format(393495974L));
        assertEquals("4.3 GB", f.format(4294967303L));
        assertEquals("1.0 TB", f.format(1000000000000L));
        assertEquals("1.1 TB", f.format(1099999999999L));
        assertEquals("2.0 TB", f.format(1999999999999L));
        assertEquals("2.0 TB", f.format(2000000000000L));
    }
}
