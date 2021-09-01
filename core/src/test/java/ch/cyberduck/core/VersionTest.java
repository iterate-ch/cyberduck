package ch.cyberduck.core;

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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VersionTest {

    @Test
    public void testCompare() {
        assertEquals(0, new Version("4").compareTo(new Version("4")));
        assertEquals(0, new Version("4.1").compareTo(new Version("4.1")));
        assertEquals(0, new Version("4.12").compareTo(new Version("4.12")));
        assertEquals(-1, new Version("4.12").compareTo(new Version("4.22")));
        assertEquals(-1, new Version("4.12.9").compareTo(new Version("4.22")));
        assertEquals(-1, new Version("4.12.9-LTS").compareTo(new Version("4.22")));
        assertEquals(1, new Version("4.22").compareTo(new Version("4.12")));
        assertEquals(1, new Version("4.22").compareTo(new Version("4.12.9")));
        assertEquals(1, new Version("4.22").compareTo(new Version("4.12.9-LTS")));
        assertEquals(-1, new Version("4.20").compareTo(new Version("4.30")));
        assertEquals(0, new Version("4.30").compareTo(new Version("4.30")));
    }
}
