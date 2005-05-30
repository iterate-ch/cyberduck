package ch.cyberduck.core;

import junit.framework.TestCase;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

public class PermissionTest extends TestCase {

    public PermissionTest(String name) {
        super(name);
    }

    public void testEmptyPermissions() {
        Permission p = new Permission();
        assertEquals(p.getMask(), "---------");
        assertTrue(p.getDecimalCode() == 0);
        assertFalse(p.getGroupPermissions()[0]);
        assertFalse(p.getGroupPermissions()[1]);
        assertFalse(p.getGroupPermissions()[2]);
        assertFalse(p.getOwnerPermissions()[0]);
        assertFalse(p.getOwnerPermissions()[1]);
        assertFalse(p.getOwnerPermissions()[2]);
        assertFalse(p.getOtherPermissions()[0]);
        assertFalse(p.getOtherPermissions()[1]);
        assertFalse(p.getOtherPermissions()[2]);
        assertEquals(p.getOctalCode(), "000");
    }
}
