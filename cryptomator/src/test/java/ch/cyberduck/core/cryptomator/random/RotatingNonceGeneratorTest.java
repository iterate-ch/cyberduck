package ch.cyberduck.core.cryptomator.random;

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

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;

public class RotatingNonceGeneratorTest {

    @Test
    public void testRotation() {
        final RotatingNonceGenerator generator = new RotatingNonceGenerator(2);
        final byte[] r1 = generator.next();
        final byte[] r2 = generator.next();
        final byte[] r3 = generator.next();
        assertFalse(Arrays.equals(r1, r2));
        assertArrayEquals(r1, r3);
    }
}