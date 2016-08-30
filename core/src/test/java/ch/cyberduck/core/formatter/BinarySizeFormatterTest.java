package ch.cyberduck.core.formatter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BinarySizeFormatterTest {

    @Test
    public void testFormat() throws Exception {
        BinarySizeFormatter f = new BinarySizeFormatter();
        assertEquals("1.0 KiB", f.format(1024));
        assertEquals("1.5 KiB", f.format(1500));
        assertEquals("1.4 KiB", f.format(1480));
        assertEquals("2.0 KiB", f.format(2000));
        assertEquals("1.0 MiB", f.format(1048576));
        assertEquals("1.0 GiB", f.format(1073741824));
        assertEquals("375.3 MiB", f.format(393495974));
    }
}
