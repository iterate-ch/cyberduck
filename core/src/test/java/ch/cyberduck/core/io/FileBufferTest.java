package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class FileBufferTest {

    @Test
    public void testTruncate() throws Exception {
        final FileBuffer buffer = new FileBuffer();
        assertEquals(0L, buffer.length(), 0L);
        final byte[] chunk = RandomUtils.nextBytes(100);
        buffer.write(chunk, 0L);
        assertEquals(100L, buffer.length(), 0L);
        buffer.truncate(1L);
        assertEquals(1L, buffer.length(), 0L);
        final byte[] read = new byte[1];
        assertEquals(1, buffer.read(read, 0L));
        assertEquals(chunk[0], read[0]);
        assertEquals(1L, buffer.length(), 0L);
    }

    @Test
    public void testSeek() throws Exception {
        final FileBuffer buffer = new FileBuffer();
        assertEquals(0L, buffer.length(), 0L);
        final byte[] chunk = RandomUtils.nextBytes(100);
        buffer.write(chunk, 0L);
        assertEquals(100L, buffer.length(), 0L);
        buffer.read(new byte[20], 1L);
        assertEquals(100L, buffer.length(), 0L);
    }

    @Test
    public void testClose() throws Exception {
        final FileBuffer buffer = new FileBuffer();
        assertEquals(0L, buffer.length(), 0L);
        final byte[] chunk = RandomUtils.nextBytes(100);
        buffer.write(chunk, 0L);
        assertEquals(100L, buffer.length(), 0L);
        buffer.close();
        assertEquals(0L, buffer.length(), 0L);
        buffer.write(chunk, 0L);
        assertEquals(100L, buffer.length(), 0L);
    }

    @Test
    public void testSetSize() throws Exception {
        final FileBuffer buffer = new FileBuffer();
        assertEquals(0L, buffer.length(), 0L);
        buffer.truncate(1L);
        assertEquals(1L, buffer.length(), 0L);
        final byte[] chunk = RandomUtils.nextBytes(100);
        buffer.write(chunk, 0L);
        assertEquals(100L, buffer.length(), 0L);
    }

    @Test
    public void testSplit() throws Exception {
        final FileBuffer buffer = new FileBuffer();
        buffer.truncate(200L);
        assertEquals(200L, buffer.length(), 0L);
        final byte[] chunk = RandomUtils.nextBytes(100);
        buffer.write(chunk, 0L);
        assertEquals(200L, buffer.length(), 0L);
        final byte[] compare = new byte[100];
        buffer.read(compare, 0L);
        final byte[] empty = new byte[100];
        buffer.read(empty, 100L);
        assertArrayEquals(new byte[100], empty);
        final byte[] overFileEnd = new byte[150];
        assertEquals(150L, buffer.read(overFileEnd, 0L));
        assertNotEquals(IOUtils.EOF, buffer.read(empty, 100L));
        assertEquals(IOUtils.EOF, buffer.read(new byte[1], 200L));
    }

    @Test
    public void testEmpty() throws Exception {
        final FileBuffer buffer = new FileBuffer();
        assertEquals(0L, buffer.length(), 0L);
        assertEquals(IOUtils.EOF, buffer.read(new byte[10], 100L));
    }
}
