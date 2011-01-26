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
 *  MERCHANTABILI
 TY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

/**
 * @version $Id$
 */
public class PermissionTest extends AbstractTestCase {

    public PermissionTest(String name) {
        super(name);
    }

    public void testClone() throws Exception {
        {
            Permission p = new Permission("rwxrwxrwx");
            assertEquals(p, new Permission(p.getAsDictionary()));
        }
        {
            Permission p = new Permission("rwx------");
            assertEquals(p, new Permission(p.getAsDictionary()));
        }
    }

    /**
     * 4000    (the set-user-ID-on-execution bit) Executable files with this bit set will run with effective uid set to the uid of the file owner.
     * Directories with the set-user-id bit set will force all files and sub-directories created in them to be owned by the directory owner
     * and not by the uid of the creating process, if the underlying file system supports this feature: see chmod(2) and the suiddir option to
     * mount(8).
     *
     * @throws Exception
     */
    public void testSetUid() throws Exception {
        {
            Permission p = new Permission(4755);
            assertTrue(p.isSetuid());
        }
    }

    /**
     * 2000    (the set-group-ID-on-execution bit) Executable files with this bit set will run with effective gid set to the gid of the file owner.
     *
     * @throws Exception
     */
    public void testSetGid() throws Exception {
        {
            Permission p = new Permission(2755);
            assertTrue(p.isSetgid());
        }
    }

    /**
     * 1000    (the sticky bit) See chmod(2) and sticky(8).
     *
     * @throws Exception
     */
    public void testSetSticky() throws Exception {
        {
            Permission p = new Permission(1755);
            assertTrue(p.isSticky());
        }
    }
}
