package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SkippingDigestInputStreamTest {

    @Test
    public void testSkipIncludedInDigest() throws Exception {
        final byte[] content = new byte[20000];
        for(int i = 0; i < content.length; i++) {
            content[i] = (byte) i;
        }
        final MessageDigest digest = MessageDigest.getInstance("MD5");
        final SkippingDigestInputStream in = new SkippingDigestInputStream(new ByteArrayInputStream(content), digest);
        assertEquals(10000L, in.skip(10000L));
        assertEquals(10000L, IOUtils.toByteArray(in).length);
        assertArrayEquals(MessageDigest.getInstance("MD5").digest(content), digest.digest());
    }

    @Test
    public void testSkipBeyondEnd() throws Exception {
        final SkippingDigestInputStream in = new SkippingDigestInputStream(new ByteArrayInputStream(new byte[10]), MessageDigest.getInstance("MD5"));
        assertEquals(10L, in.skip(100L));
    }
}
