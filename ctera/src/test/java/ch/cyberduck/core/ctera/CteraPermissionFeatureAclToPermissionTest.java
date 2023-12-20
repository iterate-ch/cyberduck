package ch.cyberduck.core.ctera;/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Acl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import java.util.Arrays;
import java.util.Collection;

import static ch.cyberduck.core.ctera.CteraCustomACL.*;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class CteraPermissionFeatureAclToPermissionTest {
    @Parameterized.Parameters(name = "{0} {1} {2} {3}")
    public static Collection<Object[]> data() {

        return Arrays.asList(
                new Object[][]{
                        {Acl.EMPTY, true, true, true},
                        {new Acl(new Acl.CanonicalUser(), writepermission), false, true, false},
                        {new Acl(new Acl.CanonicalUser(), readpermission), true, false, false},
                        {new Acl(new Acl.CanonicalUser(), executepermission), false, false, true},
                        {new Acl(new Acl.CanonicalUser(), deletepermission), false, false, false},
                        {new Acl(new Acl.CanonicalUser(), traversepermission), false, false, true},
                        {new Acl(new Acl.CanonicalUser(), Createfilepermission), false, false, false},
                        {new Acl(new Acl.CanonicalUser(), CreateDirectoriespermission), false, false, false}
                }
        );
    }

    @Parameter
    public Acl acl;

    @Parameter(1)
    public boolean r;

    @Parameter(2)
    public boolean w;

    @Parameter(3)
    public boolean x;


    @Test
    public void testAclToPermission() {
        assertEquals(r, aclToPermission(acl).isReadable());
        assertEquals(w, aclToPermission(acl).isWritable());
        assertEquals(x, aclToPermission(acl).isExecutable());
    }
}
