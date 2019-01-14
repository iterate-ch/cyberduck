package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class MD5FastChecksumComputeTest {

    @Test
    public void testCompute() throws Exception {
        assertEquals("a43c1b0aa53a0c908810c06ab1ff3967",
            new MD5FastChecksumCompute().compute(IOUtils.toInputStream("input", Charset.defaultCharset()), new TransferStatus()).hash);
    }

    @Test
    public void testComputeEmptyString() throws Exception {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e",
            new MD5FastChecksumCompute().compute(IOUtils.toInputStream("", Charset.defaultCharset()), new TransferStatus()).hash);
        assertEquals("d41d8cd98f00b204e9800998ecf8427e",
            new MD5FastChecksumCompute().compute(new NullInputStream(0L), new TransferStatus().length(0)).hash);
    }

    @Test
    public void testNormalize() throws Exception {
        assertEquals("a43c1b0aa53a0c908810c06ab1ff3967",
            new MD5FastChecksumCompute().compute(IOUtils.toInputStream("input", Charset.defaultCharset()),
                new TransferStatus()).hash);
        assertEquals("a43c1b0aa53a0c908810c06ab1ff3967",
            new MD5FastChecksumCompute().compute(IOUtils.toInputStream("_input", Charset.defaultCharset()),
                new TransferStatus().skip(1)).hash);
        assertEquals("a43c1b0aa53a0c908810c06ab1ff3967",
            new MD5FastChecksumCompute().compute(IOUtils.toInputStream("_input_", Charset.defaultCharset()),
                new TransferStatus().skip(1).length(5)).hash);
    }
}
