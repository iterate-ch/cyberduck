package ch.cyberduck.core.cdn;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.FactoryException;
import ch.cyberduck.core.Path;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class DistributionTest extends AbstractTestCase {

    @Test
    public void testEquals() throws Exception {
        assertEquals(new Distribution("o", Distribution.DOWNLOAD), new Distribution("o", Distribution.DOWNLOAD));
        assertFalse(new Distribution("o", Distribution.DOWNLOAD).equals(new Distribution("o", Distribution.STREAMING)));
        assertFalse(new Distribution("o", Distribution.DOWNLOAD).equals(new Distribution("o", Distribution.CUSTOM)));
        assertFalse(new Distribution("o", Distribution.DOWNLOAD).equals(new Distribution("o", Distribution.WEBSITE)));
        assertFalse(new Distribution("o", Distribution.DOWNLOAD).equals(new Distribution("o", Distribution.WEBSITE_CDN)));
    }

    @Test
    public void testMethods() throws Exception {
        assertEquals(Distribution.DOWNLOAD, Distribution.Method.forName(Distribution.DOWNLOAD.toString()));
        assertEquals(Distribution.CUSTOM, Distribution.Method.forName(Distribution.CUSTOM.toString()));
        assertEquals(Distribution.STREAMING, Distribution.Method.forName(Distribution.STREAMING.toString()));
        assertEquals(Distribution.WEBSITE, Distribution.Method.forName(Distribution.WEBSITE.toString()));
        assertEquals(Distribution.WEBSITE_CDN, Distribution.Method.forName(Distribution.WEBSITE_CDN.toString()));
    }

    @Test
    public void testDeployed() throws Exception {
        assertTrue(new Distribution("o", Distribution.DOWNLOAD, true).isDeployed());
    }

    @Test
    public void testCnames() throws Exception {
        assertNotNull(new Distribution("o", Distribution.DOWNLOAD, false).getCNAMEs());
    }

    @Test(expected = FactoryException.class)
    public void testMethodInvalid() throws Exception {
        Distribution.Method.forName("i");
    }

    @Test
    public void testOriginBucket() throws Exception {
        assertEquals("test.cyberduck.ch.s3.amazonaws.com",
                new Distribution("test.cyberduck.ch.s3.amazonaws.com", Distribution.DOWNLOAD).getOrigin());
        assertEquals("http://test.cyberduck.ch.s3.amazonaws.com/f",
                new Distribution("test.cyberduck.ch.s3.amazonaws.com", Distribution.DOWNLOAD).getOrigin(
                        new Path("/test.cyberduck.ch/f", Path.FILE_TYPE)
                ));
    }

    @Test
    public void testOriginCustom() throws Exception {
        assertEquals("test.cyberduck.ch",
                new Distribution("test.cyberduck.ch", Distribution.DOWNLOAD).getOrigin());
        assertEquals("http://test.cyberduck.ch/f",
                new Distribution("test.cyberduck.ch", Distribution.CUSTOM).getOrigin(
                        new Path("/f", Path.FILE_TYPE)
                ));
    }
}
