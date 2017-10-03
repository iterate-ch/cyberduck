package ch.cyberduck.core.s3;

import ch.cyberduck.core.Scheme;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class S3ProtocolTest {

    @Test
    public void testPrefix() {
        assertEquals("ch.cyberduck.core.s3.S3", new S3Protocol().getPrefix());
    }

    @Test
    public void testConfigurable() {
        assertTrue(new S3Protocol().isHostnameConfigurable());
        assertTrue(new S3Protocol().isPortConfigurable());
    }

    @Test
    public void testSchemes() {
        assertTrue(Arrays.asList(new S3Protocol().getSchemes()).contains(Scheme.s3));
        assertTrue(Arrays.asList(new S3Protocol().getSchemes()).contains(Scheme.https));
    }
}
