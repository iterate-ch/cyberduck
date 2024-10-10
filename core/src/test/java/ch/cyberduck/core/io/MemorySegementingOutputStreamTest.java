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

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MemorySegementingOutputStreamTest {

    @Test
    public void testFlush() throws Exception {
        final ByteArrayOutputStream proxy = new ByteArrayOutputStream(20);
        final MemorySegementingOutputStream out = new MemorySegementingOutputStream(proxy, 32768);
        final byte[] content = RandomUtils.insecure().nextBytes(40500);
        out.write(content, 0, 32800);
        assertEquals(32768, proxy.toByteArray().length);
        out.flush();
        assertEquals(32800, proxy.toByteArray().length);
        out.write(content, 32800, 7700);
        out.close();
        assertArrayEquals(content, proxy.toByteArray());
    }

    @Test
    public void testCopy1() throws Exception {
        final ByteArrayOutputStream proxy = new ByteArrayOutputStream(20);
        final MemorySegementingOutputStream out = new MemorySegementingOutputStream(proxy, 32768);
        final byte[] content = RandomUtils.insecure().nextBytes(40500);
        out.write(content, 0, 32800);
        assertEquals(32768, proxy.toByteArray().length);
        out.write(content, 32800, 7700);
        out.close();
        assertArrayEquals(content, proxy.toByteArray());
    }

    @Test
    public void testCopy2() throws Exception {
        final ByteArrayOutputStream proxy = new ByteArrayOutputStream(40500);
        final MemorySegementingOutputStream out = new MemorySegementingOutputStream(proxy, 32768);
        final byte[] content = RandomUtils.insecure().nextBytes(40500);
        out.write(content, 0, 32768);
        out.write(content, 32768, 7732);
        out.close();
        assertArrayEquals(content, proxy.toByteArray());
    }

    @Test
    public void testCopy3() throws Exception {
        final ByteArrayOutputStream proxy = new ByteArrayOutputStream(40500);
        final MemorySegementingOutputStream out = new MemorySegementingOutputStream(proxy, 32768);
        final byte[] content = RandomUtils.insecure().nextBytes(40500);
        out.write(content, 0, 32767);
        assertEquals(0, proxy.toByteArray().length);
        out.write(content, 32767, 2);
        assertEquals(32768, proxy.toByteArray().length);
        out.write(content, 32769, 7731);
        out.close();
        assertArrayEquals(content, proxy.toByteArray());
    }
}
