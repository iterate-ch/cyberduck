package ch.cyberduck.binding.foundation;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.library.Native;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NSUUIDTest {

    static {
        Native.load("core");
    }

    @Test
    public void testUUIDString() {
        final NSUUID uuid = NSUUID.UUID();
        assertNotNull(uuid);
        assertNotNull(uuid.UUIDString());
    }

    @Test
    public void testCustom() {
        final NSUUID uuid = NSUUID.UUID("DDB85013-288D-4870-91F1-5DD193079E9E");
        assertNotNull(uuid);
        assertEquals("DDB85013-288D-4870-91F1-5DD193079E9E", uuid.UUIDString());
    }
}