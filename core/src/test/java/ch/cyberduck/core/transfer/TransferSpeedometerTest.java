package ch.cyberduck.core.transfer;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TransferSpeedometerTest {

    @Test
    public void testReset() throws Exception {
        final DownloadTransfer transfer = new DownloadTransfer(new Host(new TestProtocol()), new Path("/p", EnumSet.of(Path.Type.file)), new Local("/t"));
        final TransferSpeedometer s = new TransferSpeedometer(transfer);
        transfer.addSize(8L);
        assertEquals(8L, s.getStatus().getSize(), 0L);
        assertEquals(0L, s.getStatus().getTransferred(), 0L);
        assertEquals(0L, s.getStatus().getSpeed(), 0L);
        s.reset();
        assertEquals(0L, s.getStatus().getSpeed(), 0L);
        transfer.addTransferred(4L);
        Thread.sleep(1000L);
        assertNotEquals(0L, s.getStatus().getSpeed(), 0L);
        s.reset();
        assertEquals(0L, s.getStatus().getSpeed(), 0L);
    }
}