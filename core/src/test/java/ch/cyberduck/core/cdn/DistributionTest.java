package ch.cyberduck.core.cdn;

import ch.cyberduck.core.FactoryException;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class DistributionTest {

    @Test
    public void testEquals() {
        assertEquals(new Distribution(Distribution.DOWNLOAD, URI.create("o"), false), new Distribution(Distribution.DOWNLOAD, URI.create("o"), false));
        assertEquals(new Distribution(Distribution.DOWNLOAD, URI.create("o"), true), new Distribution(Distribution.DOWNLOAD, URI.create("o"), false));
        assertNotEquals(new Distribution(Distribution.DOWNLOAD, URI.create("o"), false), new Distribution(Distribution.STREAMING, URI.create("o"), false));
        assertNotEquals(new Distribution(Distribution.DOWNLOAD, URI.create("o"), false), new Distribution(Distribution.CUSTOM, URI.create("o"), false));
        assertNotEquals(new Distribution(Distribution.DOWNLOAD, URI.create("o"), false), new Distribution(Distribution.WEBSITE, URI.create("o"), false));
        assertNotEquals(new Distribution(Distribution.DOWNLOAD, URI.create("o"), false), new Distribution(Distribution.WEBSITE_CDN, URI.create("o"), false));
    }

    @Test
    public void testMethods() {
        assertEquals(Distribution.DOWNLOAD, Distribution.Method.forName(Distribution.DOWNLOAD.toString()));
        assertEquals(Distribution.CUSTOM, Distribution.Method.forName(Distribution.CUSTOM.toString()));
        assertEquals(Distribution.STREAMING, Distribution.Method.forName(Distribution.STREAMING.toString()));
        assertEquals(Distribution.WEBSITE, Distribution.Method.forName(Distribution.WEBSITE.toString()));
        assertEquals(Distribution.WEBSITE_CDN, Distribution.Method.forName(Distribution.WEBSITE_CDN.toString()));
    }

    @Test
    public void testDeployed() {
        assertTrue(new Distribution(Distribution.DOWNLOAD, true).isDeployed());
    }

    @Test
    public void testCnames() {
        assertNotNull(new Distribution(Distribution.DOWNLOAD, false).getCNAMEs());
    }

    @Test(expected = FactoryException.class)
    public void testMethodInvalid() {
        Distribution.Method.forName("i");
    }

    @Test
    public void testOriginBucket() {
        assertEquals(URI.create("test.cyberduck.ch.s3.amazonaws.com"),
            new Distribution(Distribution.DOWNLOAD, URI.create("test.cyberduck.ch.s3.amazonaws.com"), false).getOrigin());
    }

    @Test
    public void testOriginCustom() {
        assertEquals(URI.create("test.cyberduck.ch"),
            new Distribution(Distribution.DOWNLOAD, URI.create("test.cyberduck.ch"), false).getOrigin());
    }
}
