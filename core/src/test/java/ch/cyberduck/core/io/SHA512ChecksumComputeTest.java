package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

public class SHA512ChecksumComputeTest {

    @Test
    public void testCompute() throws Exception {
        assertEquals("cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e",
            new SHA512ChecksumCompute().compute(new NullInputStream(0), new TransferStatus()).hash);
    }

    @Test
    public void testNormalize() throws Exception {
        assertEquals("dc6d6c30f2be9c976d6318c9a534d85e9a1c3f3608321a04b4678ef408124d45d7164f3e562e68c6c0b6c077340a785824017032fddfa924f4cf400e6cbb6adc",
            new SHA512ChecksumCompute().compute(IOUtils.toInputStream("input", Charset.defaultCharset()),
                new TransferStatus()).hash);
        assertEquals("dc6d6c30f2be9c976d6318c9a534d85e9a1c3f3608321a04b4678ef408124d45d7164f3e562e68c6c0b6c077340a785824017032fddfa924f4cf400e6cbb6adc",
            new SHA512ChecksumCompute().compute(IOUtils.toInputStream("_input", Charset.defaultCharset()),
                new TransferStatus().skip(1)).hash);
        assertEquals("dc6d6c30f2be9c976d6318c9a534d85e9a1c3f3608321a04b4678ef408124d45d7164f3e562e68c6c0b6c077340a785824017032fddfa924f4cf400e6cbb6adc",
            new SHA512ChecksumCompute().compute(IOUtils.toInputStream("_input_", Charset.defaultCharset()),
                new TransferStatus().skip(1).length(5)).hash);
    }
}
