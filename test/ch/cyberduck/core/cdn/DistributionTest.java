package ch.cyberduck.core.cdn;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.FactoryException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @version $Id$
 */
public class DistributionTest extends AbstractTestCase {

    @Test
    public void testEquals() throws Exception {
        assertEquals(new Distribution("o", Distribution.DOWNLOAD), new Distribution("o", Distribution.DOWNLOAD));
        assertFalse(new Distribution("a", Distribution.DOWNLOAD).equals(new Distribution("o", Distribution.DOWNLOAD)));
        assertFalse(new Distribution("o", Distribution.DOWNLOAD).equals(new Distribution("o", Distribution.STREAMING)));
    }

    @Test
    public void testMethods() throws Exception {
        assertEquals(Distribution.DOWNLOAD, Distribution.Method.forName(Distribution.DOWNLOAD.toString()));
        assertEquals(Distribution.CUSTOM, Distribution.Method.forName(Distribution.CUSTOM.toString()));
        assertEquals(Distribution.STREAMING, Distribution.Method.forName(Distribution.STREAMING.toString()));
        assertEquals(Distribution.WEBSITE, Distribution.Method.forName(Distribution.WEBSITE.toString()));
        assertEquals(Distribution.WEBSITE_CDN, Distribution.Method.forName(Distribution.WEBSITE_CDN.toString()));
    }

    @Test(expected = FactoryException.class)
    public void testMethodInvalid() throws Exception {
        Distribution.Method.forName("i");
    }
}
