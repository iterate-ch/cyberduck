package ch.cyberduck.core.transfer;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import org.junit.Test;

import static org.junit.Assert.*;

public class TransferStatusTest {

    @Test
    public void testSetResume() {
        TransferStatus status = new TransferStatus();
        status.setOffset(1024);
        status.setAppend(true);
        assertEquals(1024, status.getOffset());
        status.setAppend(false);
        assertEquals(0, status.getOffset());
    }

    @Test
    public void testSetComplete() throws Exception {
        TransferStatus status = new TransferStatus();
        status.setLength(1L);
        assertFalse(status.isComplete());
        status.setOffset(1L);
        assertFalse(status.isComplete());
        status.setComplete();
        assertTrue(status.isComplete());
        assertTrue(status.await());
    }

    @Test
    public void testEquals() {
        assertEquals(new TransferStatus(), new TransferStatus());
        assertEquals(new TransferStatus().hashCode(), new TransferStatus().hashCode());
    }
}