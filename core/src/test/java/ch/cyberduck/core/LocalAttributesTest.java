package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.*;

public class LocalAttributesTest {

    @Test
    public void testGetSize() throws Exception {
        assertEquals(-1, new LocalAttributes(UUID.randomUUID().toString()).getSize());
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        LocalAttributes a = new LocalAttributes(f.getAbsolutePath());
        assertEquals(0, a.getSize());
        f.delete();
    }

    @Test
    public void testGetCreationDate() throws Exception {
        assertEquals(-1, new LocalAttributes(UUID.randomUUID().toString()).getCreationDate());
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        LocalAttributes a = new LocalAttributes(f.getAbsolutePath());
        assertTrue(a.getCreationDate() > 0);
        f.delete();
    }

    @Test
    public void testGetAccessedDate() throws Exception {
        assertEquals(-1, new LocalAttributes(UUID.randomUUID().toString()).getAccessedDate());
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        LocalAttributes a = new LocalAttributes(f.getAbsolutePath());
        assertTrue(a.getAccessedDate() > 0);
        f.delete();
    }

    @Test
    public void getGetModificationDate() throws Exception {
        assertEquals(-1, new LocalAttributes(UUID.randomUUID().toString()).getModificationDate());
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        LocalAttributes a = new LocalAttributes(f.getAbsolutePath());
        assertTrue(a.getModificationDate() > 0);
        f.delete();
    }

    @Test
    public void testGetOwner() {
        LocalAttributes a = new LocalAttributes(UUID.randomUUID().toString());
        assertNull(a.getOwner());
    }

    @Test
    public void testGetGroup() {
        LocalAttributes a = new LocalAttributes(UUID.randomUUID().toString());
        assertNull(a.getGroup());
    }

    @Test
    public void testIsBundle() {
        LocalAttributes a = new LocalAttributes(UUID.randomUUID().toString());
        assertFalse(a.isBundle());
    }

    @Test
    public void testGetPermissionNotFound() {
        assertEquals(Permission.EMPTY, new LocalAttributes(UUID.randomUUID().toString()).getPermission());
    }

    @Test
    public void testGetModificationDate() throws Exception {
        assertEquals(-1, new LocalAttributes(UUID.randomUUID().toString()).getModificationDate());
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        LocalAttributes a = new LocalAttributes(f.getAbsolutePath());
        assertTrue(a.getModificationDate() > 0);
        f.delete();
    }
}
