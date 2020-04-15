package ch.cyberduck.core.googlestorage;

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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@Category(IntegrationTest.class)
public class GoogleStorageAccessControlListFeatureTest extends AbstractGoogleStorageTest {

    @Test
    public void testWrite() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        session.getFeature(Touch.class).touch(test, new TransferStatus());
        final GoogleStorageAccessControlListFeature f = new GoogleStorageAccessControlListFeature(session);
        final Acl acl = new Acl();
        acl.addAll(new Acl.GroupUser(Acl.GroupUser.EVERYONE), new Acl.Role(Acl.Role.READ));
        acl.addAll(new Acl.GroupUser(Acl.GroupUser.AUTHENTICATED), new Acl.Role(Acl.Role.READ));
        f.setPermission(test, acl);
        assertTrue(f.getPermission(test).asList().contains(new Acl.UserAndRole(new Acl.GroupUser(Acl.GroupUser.EVERYONE), new Acl.Role(Acl.Role.READ))));
        assertTrue(f.getPermission(test).asList().contains(new Acl.UserAndRole(new Acl.GroupUser(Acl.GroupUser.AUTHENTICATED), new Acl.Role(Acl.Role.READ))));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadBucket() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory));
        final GoogleStorageAccessControlListFeature f = new GoogleStorageAccessControlListFeature(session);
        final Acl acl = f.getPermission(container);
        assertTrue(acl.containsKey(new Acl.GroupUser("cloud-storage-analytics@google.com")));
        //assertTrue(acl.containsKey(new Acl.GroupUser(acl.getOwner().getIdentifier())));
        assertFalse(acl.containsKey(new Acl.GroupUser(Acl.GroupUser.EVERYONE)));
    }

    @Test
    public void testRoles() {
        final GoogleStorageAccessControlListFeature f = new GoogleStorageAccessControlListFeature(session);
        final List<Acl.User> users = f.getAvailableAclUsers();
        assertTrue(users.contains(new Acl.CanonicalUser()));
        assertTrue(users.contains(new Acl.EmailUser()));
        assertTrue(users.contains(new Acl.EmailGroupUser("")));
        assertTrue(users.contains(new Acl.DomainUser("")));
    }
}
