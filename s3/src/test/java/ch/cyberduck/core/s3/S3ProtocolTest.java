package ch.cyberduck.core.s3;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class S3ProtocolTest {

    @Test
    public void testPrefix() {
        assertEquals("ch.cyberduck.core.s3.S3", new S3Protocol().getPrefix());
    }
}