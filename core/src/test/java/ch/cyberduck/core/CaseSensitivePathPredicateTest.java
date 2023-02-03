package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import java.util.EnumSet;

import static org.junit.Assert.*;

public class CaseSensitivePathPredicateTest {

    @Test
    public void testPredicateTest() {
        final Path t = new Path("/f", EnumSet.of(Path.Type.file));
        assertTrue(new CaseSensitivePathPredicate(t).test(new Path("/f", EnumSet.of(Path.Type.file))));
        assertEquals(new CaseSensitivePathPredicate(t).hashCode(), new CaseSensitivePathPredicate(new Path("/f", EnumSet.of(Path.Type.file))).hashCode());
        assertFalse(new CaseSensitivePathPredicate(t).test(new Path("/F", EnumSet.of(Path.Type.file))));
        assertNotEquals(new CaseSensitivePathPredicate(t).hashCode(), new CaseSensitivePathPredicate(new Path("/F", EnumSet.of(Path.Type.file))).hashCode());
        assertFalse(new CaseSensitivePathPredicate(t).test(new Path("/f", EnumSet.of(Path.Type.directory))));
    }
}