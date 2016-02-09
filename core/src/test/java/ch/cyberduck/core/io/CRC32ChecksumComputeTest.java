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

import org.apache.commons.io.input.NullInputStream;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CRC32ChecksumComputeTest {

    @Test
    public void testCompute() throws Exception {
        assertEquals("0",
                new CRC32ChecksumCompute().compute(new NullInputStream(0)).hash);
        assertEquals("d202ef8d",
                new CRC32ChecksumCompute().compute(new NullInputStream(1L)).hash);
    }
}