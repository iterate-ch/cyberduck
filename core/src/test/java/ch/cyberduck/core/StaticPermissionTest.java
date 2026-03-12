package ch.cyberduck.core;

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

import ch.cyberduck.core.serializer.PermissionDictionary;

import org.junit.Test;

import static org.junit.Assert.*;

public class StaticPermissionTest {

    @Test
    public void testGetAsDictionary() {
        assertEquals(new StaticPermission(777), new PermissionDictionary<>().deserialize(new StaticPermission(777).serialize(SerializerFactory.get())));
        assertEquals(new StaticPermission(700), new PermissionDictionary<>().deserialize(new StaticPermission(700).serialize(SerializerFactory.get())));
        assertEquals(new StaticPermission(400), new PermissionDictionary<>().deserialize(new StaticPermission(400).serialize(SerializerFactory.get())));
    }

    @Test
    public void testSymbol() {
        Permission p1 = new StaticPermission(777);
        assertEquals("rwxrwxrwx", p1.getSymbol());
        Permission p2 = new StaticPermission(666);
        assertEquals("rw-rw-rw-", p2.getSymbol());
    }

    /**
     * 4000    (the set-user-ID-on-execution bit) Executable files with this bit set will run with effective uid set to the uid of the file owner.
     * Directories with the set-user-id bit set will force all files and sub-directories created in them to be owned by the directory owner
     * and not by the uid of the creating process, if the underlying file system supports this feature: see chmod(2) and the suiddir option to
     * mount(8).
     */
    @Test
    public void testSetUid() {
        assertTrue(new StaticPermission(Permission.Action.read, Permission.Action.none, Permission.Action.none,
                false, true, false).isSetuid());
        assertTrue(new StaticPermission(4755).isSetuid());
        assertTrue(new StaticPermission(6755).isSetuid());
        assertTrue(new StaticPermission(5755).isSetuid());
        assertFalse(new StaticPermission(1755).isSetuid());
        assertEquals("--S------", new StaticPermission(4000).getSymbol());
        assertEquals("4000", new StaticPermission(4000).getMode());
    }

    /**
     * 2000    (the set-group-ID-on-execution bit) Executable files with this bit set will run with effective gid set to the gid of the file owner.
     */
    @Test
    public void testSetGid() {
        assertTrue(new StaticPermission(Permission.Action.read, Permission.Action.none, Permission.Action.none,
                false, false, true).isSetgid());
        assertTrue(new StaticPermission(2755).isSetgid());
        assertTrue(new StaticPermission(3755).isSetgid());
        assertTrue(new StaticPermission(6755).isSetgid());
        assertFalse(new StaticPermission(1755).isSetgid());
        assertEquals("-----S---", new StaticPermission(2000).getSymbol());
        assertEquals("2000", new StaticPermission(2000).getMode());
    }

    /**
     * 1000    (the sticky bit) See chmod(2) and sticky(8).
     */
    @Test
    public void testSetSticky() {
        assertTrue(new StaticPermission(1755).isSticky());
        assertTrue(new StaticPermission(3755).isSticky());
        assertTrue(new StaticPermission(5755).isSticky());
        assertFalse(new StaticPermission(2755).isSticky());
        assertFalse(new StaticPermission(6755).isSticky());
        assertEquals("1000", new StaticPermission(1000).getMode());
        assertEquals("--------T", new StaticPermission(1000).getSymbol());
    }

    @Test
    public void testActions() {
        assertEquals(Permission.Action.read_write, Permission.Action.all.and(Permission.Action.execute.not()));
        assertEquals(Permission.Action.read, Permission.Action.none.or(Permission.Action.read));
    }

    @Test
    public void testToMode() {
        final Permission permission = new StaticPermission(Permission.Action.read,
                Permission.Action.none, Permission.Action.none);
        assertEquals("400", permission.getMode());
    }

    @Test
    public void testFromMode() {
        assertEquals(Permission.Action.all, (new StaticPermission("rwxrwxrwx").getUser()));
        assertEquals(Permission.Action.all, (new StaticPermission("rwxrwxrwx").getGroup()));
        assertEquals(Permission.Action.all, (new StaticPermission("rwxrwxrwx").getOther()));
        assertEquals(Permission.Action.all, (new StaticPermission("rwxrwxrwt").getOther()));
        assertEquals(Permission.Action.read_write, (new StaticPermission("rwxrwxrwT").getOther()));
        assertEquals(Permission.Action.read, (new StaticPermission("r--r--r--").getUser()));
        assertEquals(Permission.Action.read, (new StaticPermission("s--r--r--").getUser()));
        assertEquals(Permission.Action.none, (new StaticPermission("S--r--r--").getUser()));
        assertEquals(Permission.Action.read, (new StaticPermission("r--r--r--").getGroup()));
        assertEquals(Permission.Action.read, (new StaticPermission("r--r--r--").getOther()));
        assertEquals(Permission.Action.read_write, (new StaticPermission("rw-rw-rw-").getUser()));
        assertEquals(Permission.Action.read_write, (new StaticPermission("rw-rw-rw-").getGroup()));
        assertEquals(Permission.Action.read_write, (new StaticPermission("rw-rw-rw-").getOther()));
        assertEquals(Permission.Action.read_execute, (new StaticPermission("r-xr-xr-x").getUser()));
        assertEquals(Permission.Action.read_execute, (new StaticPermission("r-xr-xr-x").getGroup()));
        assertEquals(Permission.Action.read_execute, (new StaticPermission("r-xr-xr-x").getOther()));
    }

    @Test
    public void testModeStickyBit() {
        final Permission permission = new StaticPermission(Permission.Action.read,
                Permission.Action.none, Permission.Action.none, true, false, false);
        assertEquals("1400", permission.getMode());
    }

    @Test
    public void testFailureParsing() {
        assertEquals(Permission.EMPTY, new StaticPermission("rwx"));
        assertEquals(Permission.EMPTY, new StaticPermission(888));
    }

    @Test
    public void testEmpty() {
        assertEquals(Permission.EMPTY, new StaticPermission());
        assertEquals(Permission.EMPTY, new StaticPermission(0));
        assertTrue(Permission.EMPTY.isReadable());
        assertTrue(Permission.EMPTY.isWritable());
        assertTrue(Permission.EMPTY.isExecutable());
    }

    @Test
    public void testInit() {
        assertEquals(new StaticPermission(1000), new StaticPermission("--------T"));
        assertEquals(new StaticPermission(2000), new StaticPermission("-----S---"));
        assertEquals(new StaticPermission(2010), new StaticPermission("-----s---"));
        assertEquals(new StaticPermission(4000), new StaticPermission("--S------"));
        assertEquals(new StaticPermission(4100), new StaticPermission("--s------"));
    }

    @Test
    public void testInitMode() {
        assertEquals(new StaticPermission("rwxr-x---"), new StaticPermission("0750"));
    }

    @Test
    public void testImplies() {
        assertTrue(new StaticPermission("r--------").getUser().implies(Permission.Action.read));
        assertTrue(new StaticPermission("r-x------").getUser().implies(Permission.Action.execute));
        assertTrue(new StaticPermission("r-s------").getUser().implies(Permission.Action.execute));
        assertFalse(new StaticPermission("r-S------").getUser().implies(Permission.Action.execute));
        assertTrue(new StaticPermission("r--------").isReadable());
        assertFalse(new StaticPermission("r--------").isWritable());
        assertTrue(new StaticPermission("-w-------").getUser().implies(Permission.Action.write));
        assertTrue(new StaticPermission("-w-------").isWritable());
        assertTrue(new StaticPermission("--x------").getUser().implies(Permission.Action.execute));
        assertTrue(new StaticPermission("--x------").isExecutable());
        assertTrue(new StaticPermission("---r-----").getGroup().implies(Permission.Action.read));
        assertTrue(new StaticPermission("---r-x---").getGroup().implies(Permission.Action.execute));
        assertTrue(new StaticPermission("---r-s---").getGroup().implies(Permission.Action.execute));
        assertFalse(new StaticPermission("---r-S---").getGroup().implies(Permission.Action.execute));
        assertTrue(new StaticPermission("--------x").getOther().implies(Permission.Action.execute));
        assertTrue(new StaticPermission("--------t").getOther().implies(Permission.Action.execute));
        assertFalse(new StaticPermission("--------T").getOther().implies(Permission.Action.execute));
    }
}
