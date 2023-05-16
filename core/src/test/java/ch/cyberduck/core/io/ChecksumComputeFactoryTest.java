package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ChecksumComputeFactoryTest {

    @Test
    public void testCreate() {
        assertNotNull(ChecksumComputeFactory.get(HashAlgorithm.md5));
        assertNotNull(ChecksumComputeFactory.get(HashAlgorithm.sha1));
        assertNotNull(ChecksumComputeFactory.get(HashAlgorithm.sha256));
        assertNotNull(ChecksumComputeFactory.get(HashAlgorithm.sha512));
        assertNotNull(ChecksumComputeFactory.get(HashAlgorithm.crc32));
    }
}