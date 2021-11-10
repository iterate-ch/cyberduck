package ch.cyberduck.core.formatter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BinarySizeFormatterTest {

    @Test
    public void testFormat() {
        BinarySizeFormatter f = new BinarySizeFormatter();
        assertEquals("1.0 KiB", f.format(1024L));
        assertEquals("1.5 KiB", f.format(1500L));
        assertEquals("1.4 KiB", f.format(1480L));
        assertEquals("2.0 KiB", f.format(2000L));
        assertEquals("1.0 MiB", f.format(1048576L));
        assertEquals("1.0 GiB", f.format(1073741824L));
        assertEquals("375.3 MiB", f.format(393495974L));
        assertEquals("4.0 GiB", f.format(4294967303L));
    }
}
