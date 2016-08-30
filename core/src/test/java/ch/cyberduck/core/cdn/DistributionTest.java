package ch.cyberduck.core.cdn;

import ch.cyberduck.core.FactoryException;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class DistributionTest {

    @Test
    public void testEquals() throws Exception {
        assertEquals(new Distribution(URI.create("o"), Distribution.DOWNLOAD, false), new Distribution(URI.create("o"), Distribution.DOWNLOAD, false));
        assertFalse(new Distribution(URI.create("o"), Distribution.DOWNLOAD, false).equals(new Distribution(URI.create("o"), Distribution.STREAMING, false)));
        assertFalse(new Distribution(URI.create("o"), Distribution.DOWNLOAD, false).equals(new Distribution(URI.create("o"), Distribution.CUSTOM, false)));
        assertFalse(new Distribution(URI.create("o"), Distribution.DOWNLOAD, false).equals(new Distribution(URI.create("o"), Distribution.WEBSITE, false)));
        assertFalse(new Distribution(URI.create("o"), Distribution.DOWNLOAD, false).equals(new Distribution(URI.create("o"), Distribution.WEBSITE_CDN, false)));
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
        assertTrue(new Distribution(Distribution.DOWNLOAD, true).isDeployed());
    }

    @Test
    public void testCnames() throws Exception {
        assertNotNull(new Distribution(Distribution.DOWNLOAD, false).getCNAMEs());
    }

    @Test(expected = FactoryException.class)
    public void testMethodInvalid() throws Exception {
        Distribution.Method.forName("i");
    }

    @Test
    public void testOriginBucket() throws Exception {
        assertEquals(URI.create("test.cyberduck.ch.s3.amazonaws.com"),
                new Distribution(URI.create("test.cyberduck.ch.s3.amazonaws.com"), Distribution.DOWNLOAD, false).getOrigin());
    }

    @Test
    public void testOriginCustom() throws Exception {
        assertEquals(URI.create("test.cyberduck.ch"),
                new Distribution(URI.create("test.cyberduck.ch"), Distribution.DOWNLOAD, false).getOrigin());
    }
}
