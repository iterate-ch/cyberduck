package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

public class SHA256ChecksumComputeTest {

    @Test
    public void testCompute() throws Exception {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            new SHA256ChecksumCompute().compute(new NullInputStream(0), new TransferStatus()).hash);
    }

    @Test
    public void testNormalize() throws Exception {
        assertEquals("c96c6d5be8d08a12e7b5cdc1b207fa6b2430974c86803d8891675e76fd992c20",
            new SHA256ChecksumCompute().compute(IOUtils.toInputStream("input", Charset.defaultCharset()),
                new TransferStatus()).hash);
        assertEquals("c96c6d5be8d08a12e7b5cdc1b207fa6b2430974c86803d8891675e76fd992c20",
            new SHA256ChecksumCompute().compute(IOUtils.toInputStream("_input", Charset.defaultCharset()),
                new TransferStatus().skip(1)).hash);
        assertEquals("c96c6d5be8d08a12e7b5cdc1b207fa6b2430974c86803d8891675e76fd992c20",
            new SHA256ChecksumCompute().compute(IOUtils.toInputStream("_input_", Charset.defaultCharset()),
                new TransferStatus().skip(1).length(5)).hash);
    }
}
