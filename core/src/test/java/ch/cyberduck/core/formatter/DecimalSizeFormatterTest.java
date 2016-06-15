package ch.cyberduck.core.formatter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DecimalSizeFormatterTest {

    @Test
    public void testFormat() throws Exception {
        DecimalSizeFormatter f = new DecimalSizeFormatter();
        assertEquals("1.0 KB", f.format(1024));
        assertEquals("1.5 KB", f.format(1500));
        assertEquals("2.0 KB", f.format(2000));
        assertEquals("1.0 MB", f.format(1048576));
        assertEquals("1.1 GB", f.format(1073741824));
        assertEquals("393.5 MB", f.format(393495974));
    }
}
