package ch.cyberduck.core.http;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HttpRangeTest {

    @Test
    public void testByLength0() {
        final HttpRange range = HttpRange.byLength(0L, 0L);
        assertEquals(0L, range.getStart(), 0L);
        assertEquals(TransferStatus.UNKNOWN_LENGTH, range.getEnd(), 0L);
        assertEquals(TransferStatus.UNKNOWN_LENGTH, range.getLength(), 0L);
    }

    @Test
    public void testByLength1() {
        final HttpRange range = HttpRange.byLength(0L, 1L);
        assertEquals(0L, range.getStart(), 0L);
        assertEquals(0L, range.getEnd(), 0L);
        assertEquals(1L, range.getLength(), 0L);
    }

    @Test
    public void testByLength2() {
        final HttpRange range = HttpRange.byLength(0L, 2L);
        assertEquals(0L, range.getStart(), 0L);
        assertEquals(1L, range.getEnd(), 0L);
        assertEquals(2L, range.getLength(), 0L);
    }

    @Test
    public void testByLength3() {
        final HttpRange range = HttpRange.byLength(0L, 3L);
        assertEquals(0L, range.getStart(), 0L);
        assertEquals(2L, range.getEnd(), 0L);
        assertEquals(3L, range.getLength(), 0L);
    }

    @Test
    public void testByLength2Offset1() {
        final HttpRange range = HttpRange.byLength(1L, 2L);
        assertEquals(1L, range.getStart(), 0L);
        assertEquals(2L, range.getEnd(), 0L);
        assertEquals(2L, range.getLength(), 0L);
    }
}
