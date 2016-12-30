package ch.cyberduck.core.io;

import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

public class MD5ChecksumComputeTest {

    @Test
    public void testCompute() throws Exception {
        assertEquals("a43c1b0aa53a0c908810c06ab1ff3967",
                new MD5ChecksumCompute().compute(null, IOUtils.toInputStream("input", Charset.defaultCharset()), new TransferStatus()).hash);
    }

    @Test
    public void testComputeEmptyString() throws Exception {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e",
                new MD5ChecksumCompute().compute(null, IOUtils.toInputStream("", Charset.defaultCharset()), new TransferStatus()).hash);
    }
}
