package ch.cyberduck.core.features;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AlgorithmTest {

    @Test
    public void testFromString() throws Exception {
        assertEquals(new Encryption.Algorithm("aws:kms", null), Encryption.Algorithm.fromString("aws:kms"));
        assertEquals(new Encryption.Algorithm("aws:kms", "k"), Encryption.Algorithm.fromString("aws:kms|k"));
        assertEquals(new Encryption.Algorithm("AES256", null), Encryption.Algorithm.fromString("AES256"));
        assertEquals(new Encryption.Algorithm("a", "b"), Encryption.Algorithm.fromString("a|b"));
        assertEquals(Encryption.Algorithm.NONE, Encryption.Algorithm.fromString("none"));
    }

}