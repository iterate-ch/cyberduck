package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import org.apache.commons.io.input.NullInputStream;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SHA1ChecksumComputeTest {

    @Test
    public void testCompute() throws Exception {
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709",
                new SHA1ChecksumCompute().compute(new NullInputStream(0), new TransferStatus()).hash);

    }
}