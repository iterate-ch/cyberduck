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

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ChecksumTest extends AbstractTestCase {

    @Test
    public void testParse() throws Exception {
        assertEquals(new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427e"),
                Checksum.parse("d41d8cd98f00b204e9800998ecf8427e"));
        assertEquals(new Checksum(HashAlgorithm.sha1, "da39a3ee5e6b4b0d3255bfef95601890afd80709"),
                Checksum.parse("da39a3ee5e6b4b0d3255bfef95601890afd80709"));
        assertEquals(new Checksum(HashAlgorithm.sha256, "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"),
                Checksum.parse("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
        assertNull(
                Checksum.parse("da39a3ee5e6b4b0d3255bfef95601890afd80709-2"));
    }
}