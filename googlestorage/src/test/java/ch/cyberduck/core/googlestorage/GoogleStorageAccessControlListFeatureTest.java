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
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.*;


@Category(IntegrationTest.class)
public class GoogleStorageAccessControlListFeatureTest extends AbstractGoogleStorageTest {

    @Test
    public void testWrite() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GoogleStorageTouchFeature(session).touch(test, new TransferStatus());
        final GoogleStorageAccessControlListFeature f = new GoogleStorageAccessControlListFeature(session);
        final Acl acl = f.getPermission(test);
        acl.addAll(new Acl.GroupUser(Acl.GroupUser.EVERYONE), new Acl.Role(Acl.Role.READ));
        acl.addAll(new Acl.GroupUser(Acl.GroupUser.AUTHENTICATED), new Acl.Role(Acl.Role.READ));
        f.setPermission(test, acl);
        assertTrue(f.getPermission(test).asList().contains(new Acl.UserAndRole(new Acl.GroupUser(Acl.GroupUser.EVERYONE), new Acl.Role(Acl.Role.READ))));
        assertTrue(f.getPermission(test).asList().contains(new Acl.UserAndRole(new Acl.GroupUser(Acl.GroupUser.AUTHENTICATED), new Acl.Role(Acl.Role.READ))));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadBucketAnalayticsAcl() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory));
        final GoogleStorageAccessControlListFeature f = new GoogleStorageAccessControlListFeature(session);
        final Acl acl = f.getPermission(container);
        assertTrue(acl.asList().stream().filter(user -> user.getUser().getIdentifier().equals("cloud-storage-analytics@google.com")).findAny().isPresent());
        assertFalse(acl.containsKey(new Acl.GroupUser(Acl.GroupUser.EVERYONE)));
    }

    @Test
    public void testReadBucketUniformBucketLevelAccess() throws Exception {
        final Path container = new Path("cyberduck-test-eu-uniform-access", EnumSet.of(Path.Type.directory));
        final GoogleStorageAccessControlListFeature f = new GoogleStorageAccessControlListFeature(session);
        final Acl acl = f.getPermission(container);
        assertEquals(Acl.EMPTY, acl);
        assertEquals(Acl.EMPTY, f.getDefault(container, null));
        final Path test = new GoogleStorageTouchFeature(session).touch(new Path(new Path(container,
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(Acl.EMPTY, f.getPermission(test));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadWithDelimiter() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory));
        final Path test = new GoogleStorageTouchFeature(session).touch(new Path(new Path(container,
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final GoogleStorageAccessControlListFeature f = new GoogleStorageAccessControlListFeature(session);
        assertNotNull(f.getPermission(test));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadDirectoryPlaceholder() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory));
        final Path placeholder = new GoogleStorageDirectoryFeature(session).mkdir(new Path(container,
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final GoogleStorageAccessControlListFeature f = new GoogleStorageAccessControlListFeature(session);
        assertNotNull(f.getPermission(placeholder));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final GoogleStorageAccessControlListFeature f = new GoogleStorageAccessControlListFeature(session);
        f.getPermission(test);
    }

    @Test(expected = NotfoundException.class)
    public void testWriteNotFound() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final GoogleStorageAccessControlListFeature f = new GoogleStorageAccessControlListFeature(session);
        final Acl acl = new Acl();
        acl.addAll(new Acl.GroupUser(Acl.GroupUser.EVERYONE), new Acl.Role(Acl.Role.READ));
        f.setPermission(test, acl);
    }

    @Test
    public void testRoles() {
        final GoogleStorageAccessControlListFeature f = new GoogleStorageAccessControlListFeature(session);
        final List<Acl.User> users = f.getAvailableAclUsers();
        assertTrue(f.getAvailableAclUsers().stream().filter(user -> user instanceof Acl.CanonicalUser).findAny().isPresent());
        assertTrue(f.getAvailableAclUsers().stream().filter(user -> user instanceof Acl.EmailUser).findAny().isPresent());
        assertTrue(f.getAvailableAclUsers().stream().filter(user -> user instanceof Acl.EmailGroupUser).findAny().isPresent());
        assertTrue(f.getAvailableAclUsers().stream().filter(user -> user instanceof Acl.DomainUser).findAny().isPresent());
    }
}
