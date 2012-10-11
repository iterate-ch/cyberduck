package ch.cyberduck.ui.formatter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class BinarySizeFormatterTest {

    @Test
    public void testFormat() throws Exception {
        DecimalSizeFormatter f = new DecimalSizeFormatter();
        assertEquals("1.0 KB", f.format(1024));
        assertEquals("1.4 KB", f.format(1500));
        assertEquals("1.9 KB", f.format(2000));
        assertEquals("1.0 MB", f.format(1048576));
        assertEquals("1.0 GB", f.format(1073741824));
        assertEquals("375.2 MB", f.format(393495974));
    }
}
