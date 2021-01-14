package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import static org.junit.Assert.assertNull;

public class LinuxPasswordStoreTest {

    @Test
    public void testFindGenericPassword() throws Exception {
        final LinuxPasswordStore k = new LinuxPasswordStore();
        k.deletePassword("cyberduck.ch", "u");
        assertNull(k.getPassword("cyberduck.ch", "u"));
        k.addPassword("cyberduck.ch", "u", "s");
        assertEquals("s", k.getPassword("cyberduck.ch", "u"));
        // Duplicate
        k.addPassword("cyberduck.ch", "u", "s");
        assertEquals("s", k.getPassword("cyberduck.ch", "u"));
        k.deletePassword("cyberduck.ch", "u");
        assertNull(k.getPassword("cyberduck.ch", "u"));
    }

    @Test
    public void testFindInternetPassword() throws Exception {
        final LinuxPasswordStore k = new LinuxPasswordStore();
        k.deletePassword(Scheme.http, 80, "cyberduck.ch", "u");
        assertNull(k.getPassword(Scheme.http, 80, "cyberduck.ch", "u"));
        k.addPassword(Scheme.http, 80, "cyberduck.ch", "u", "s");
        assertEquals("s", k.getPassword(Scheme.http, 80, "cyberduck.ch", "u"));
        // Duplicate
        k.addPassword(Scheme.http, 80, "cyberduck.ch", "u", "s");
        assertEquals("s", k.getPassword(Scheme.http, 80, "cyberduck.ch", "u"));
        k.deletePassword(Scheme.http, 80, "cyberduck.ch", "u");
        assertNull(k.getPassword(Scheme.http, 80, "cyberduck.ch", "u"));
    }
}
