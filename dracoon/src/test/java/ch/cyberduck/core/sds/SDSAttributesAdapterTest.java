package ch.cyberduck.core.sds;

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

import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.io.swagger.client.model.NodePermissions;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SDSAttributesAdapterTest extends AbstractSDSTest {

    @Test
    public void testPermissionsFile() throws Exception {
        final SDSAttributesAdapter f = new SDSAttributesAdapter(session);
        final Node node = new Node();
        node.setIsEncrypted(false);
        node.setType(Node.TypeEnum.FILE);
        final NodePermissions permissions = new NodePermissions().read(false).delete(false).change(false).create(false);
        node.setPermissions(permissions);
        assertFalse(f.toPermission(node).isReadable());
        assertFalse(f.toPermission(node).isWritable());
        assertFalse(f.toPermission(node).isExecutable());
        permissions.setRead(true);
        assertTrue(f.toPermission(node).isReadable());
        permissions.setChange(true);
        assertTrue(f.toPermission(node).isReadable());
        assertFalse(f.toPermission(node).isWritable());
        permissions.setDelete(true);
        assertTrue(f.toPermission(node).isReadable());
        assertTrue(f.toPermission(node).isWritable());
        permissions.setCreate(true);
        assertTrue(f.toPermission(node).isReadable());
        assertTrue(f.toPermission(node).isWritable());
        f.toPermission(node);
    }

    @Test
    public void testPermissionsFolder() throws Exception {
        final SDSAttributesAdapter f = new SDSAttributesAdapter(session);
        final Node node = new Node();
        node.setIsEncrypted(false);
        node.setType(Node.TypeEnum.FOLDER);
        final NodePermissions permissions = new NodePermissions().read(false).delete(false).change(false).create(false);
        node.setPermissions(permissions);
        assertTrue(f.toPermission(node).isReadable());
        assertFalse(f.toPermission(node).isWritable());
        assertTrue(f.toPermission(node).isExecutable());
        permissions.setRead(true);
        assertTrue(f.toPermission(node).isReadable());
        assertFalse(f.toPermission(node).isWritable());
        assertTrue(f.toPermission(node).isExecutable());
        permissions.setChange(true);
        assertTrue(f.toPermission(node).isReadable());
        assertFalse(f.toPermission(node).isWritable());
        assertTrue(f.toPermission(node).isExecutable());
        permissions.setDelete(true);
        assertTrue(f.toPermission(node).isReadable());
        assertFalse(f.toPermission(node).isWritable());
        assertTrue(f.toPermission(node).isExecutable());
        permissions.setCreate(true);
        assertTrue(f.toPermission(node).isReadable());
        assertTrue(f.toPermission(node).isWritable());
        assertTrue(f.toPermission(node).isExecutable());
    }
}