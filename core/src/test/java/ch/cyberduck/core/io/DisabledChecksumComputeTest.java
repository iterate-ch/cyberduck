package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import org.apache.commons.io.input.NullInputStream;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DisabledChecksumComputeTest {

    @Test
    public void compute() throws Exception {
        final NullInputStream in = new NullInputStream(0L);
        new DisabledChecksumCompute().compute(in, new TransferStatus());
        assertEquals(-1, in.read());
        assertEquals(-1, in.read());
    }
}