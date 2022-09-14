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
        assertEquals("5.0 GiB", f.format(5368709120L));
        assertEquals("10.0 GiB", f.format(10737418240L));
        assertEquals("50.0 GiB", f.format(53687091200L));
        assertEquals("100.0 GiB", f.format(107374182400L));
        assertEquals("200.0 GiB", f.format(214748364800L));
        assertEquals("500.0 GiB", f.format(536870912000L));
        assertEquals("1000.0 GiB", f.format(1073741824000L));
        assertEquals("1.0 TiB", f.format(1099511627776L));
        assertEquals("2.0 TiB", f.format(2199023255552L));
        assertEquals("2.0 TiB", f.format(2147483648000L));
    }
}
