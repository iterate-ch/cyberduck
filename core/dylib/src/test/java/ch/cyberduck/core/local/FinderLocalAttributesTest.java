/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

package ch.cyberduck.core.local;

import ch.cyberduck.core.Permission;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class FinderLocalAttributesTest {

    @Test
    public void testGetSize() throws Exception {
        assertEquals(-1, new FinderLocalAttributes(new FinderLocal(UUID.randomUUID().toString())).getSize());
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        FinderLocalAttributes a = new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath()));
        assertEquals(0, a.getSize());
        f.delete();
    }

    @Test
    public void testGetPermission() throws Exception {
        assertEquals(Permission.EMPTY, new FinderLocalAttributes(new FinderLocal(UUID.randomUUID().toString())).getPermission());
    }

    @Test
    public void testGetCreationDate() throws Exception {
        assertEquals(-1, new FinderLocalAttributes(new FinderLocal(UUID.randomUUID().toString())).getCreationDate());
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        FinderLocalAttributes a = new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath()));
        Assert.assertTrue(a.getCreationDate() > 0);
        f.delete();
    }

    @Test
    public void testGetAccessedDate() throws Exception {
        assertEquals(-1, new FinderLocalAttributes(new FinderLocal(UUID.randomUUID().toString())).getAccessedDate());
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        FinderLocalAttributes a = new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath()));
        Assert.assertTrue(a.getAccessedDate() > 0);
        f.delete();
    }

    @Test
    public void getGetModificationDate() throws Exception {
        assertEquals(-1, new FinderLocalAttributes(new FinderLocal(UUID.randomUUID().toString())).getModificationDate());
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        FinderLocalAttributes a = new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath()));
        Assert.assertTrue(a.getModificationDate() > 0);
        f.delete();
    }

    @Test
    public void testGetOwner() throws Exception {
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        FinderLocalAttributes a = new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath()));
        Assert.assertNotNull(a.getOwner());
        f.delete();
    }

    @Test
    public void testGetGroup() throws Exception {
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        FinderLocalAttributes a = new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath()));
        Assert.assertNotNull(a.getGroup());
        f.delete();
    }

    @Test
    public void testGetInode() throws Exception {
        Assert.assertNull(new FinderLocalAttributes(new FinderLocal(UUID.randomUUID().toString())).getInode());
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        FinderLocalAttributes a = new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath()));
        Assert.assertTrue(a.getInode() > 0);
        f.delete();
    }

    @Test
    public void testIsBundle() throws Exception {
        FinderLocalAttributes a = new FinderLocalAttributes(new FinderLocal(UUID.randomUUID().toString()));
        Assert.assertFalse(a.isBundle());
    }

    @Test
    public void testIsSymbolicLink() throws Exception {
        Assert.assertFalse(new FinderLocalAttributes(new FinderLocal(UUID.randomUUID().toString())).isSymbolicLink());
        Assert.assertTrue(new FinderLocalAttributes(new FinderLocal("/tmp")).isSymbolicLink());
    }

    @Test
    public void testPermission() throws Exception {
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        Assert.assertTrue(new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath())).getPermission().isReadable());
        Assert.assertTrue(new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath())).getPermission().isWritable());
        Assert.assertFalse(new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath())).getPermission().isExecutable());
        f.delete();
        Assert.assertTrue(new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath())).getPermission().isReadable());
        Assert.assertTrue(new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath())).getPermission().isWritable());
        Assert.assertTrue(new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath())).getPermission().isExecutable());
    }
}
