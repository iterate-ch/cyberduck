package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.model.StorageOwner;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3AccessControlListFeatureTest extends AbstractS3Test {

    @Test
    public void testBucketDefault() throws Exception {
        final Path container = new Path("test", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        assertEquals(Acl.EMPTY, f.getDefault(container));
    }

    @Test
    public void testReadBucketDisabledAcl() throws Exception {
        final Path container = new Path("test-eu-central-1-acl-disabled", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        assertNotEquals(Acl.EMPTY, f.getPermission(container));
        assertEquals(Acl.EMPTY, f.getDefault(container));
    }

    @Test
    public void testBucketOwnerEnforced() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck-ownerenforced", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        // ACLs are disabled for objects in bucket with owner enforced
        assertEquals(Acl.EMPTY, f.getDefault(file));
        // Bucket ACL is still read but cannot be edited
        final Acl bucketAcl = f.getPermission(container);
        assertNotEquals(Acl.EMPTY, bucketAcl);
        assertFalse(bucketAcl.isEditable());
        // Reading object ACL is skipped
        new S3TouchFeature(session).touch(new S3WriteFeature(session, f), file, new TransferStatus());
        assertEquals(Acl.EMPTY, f.getPermission(file));
        new S3DefaultDeleteFeature(session, f).delete(Collections.singletonList(file), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testReadContainer() throws Exception {
        final Path container = new Path("test-acl-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Acl acl = new S3AccessControlListFeature(session).getPermission(container);
        assertTrue(acl.containsKey(new Acl.GroupUser("http://acs.amazonaws.com/groups/s3/LogDelivery")));
        assertTrue(acl.get(new Acl.GroupUser("http://acs.amazonaws.com/groups/s3/LogDelivery")).contains(
                new Acl.Role(Acl.Role.WRITE)
        ));
        assertTrue(acl.get(new Acl.GroupUser("http://acs.amazonaws.com/groups/s3/LogDelivery")).contains(
                new Acl.Role("READ_ACP")
        ));
        assertTrue(acl.containsKey(new Acl.Owner("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6")));
        assertTrue(acl.get(new Acl.Owner("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6")).contains(
                new Acl.Role(Acl.Role.FULL)
        ));
    }

    @Test
    public void testReadKey() throws Exception {
        final Path container = new Path("test-acl-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Acl acl = new S3AccessControlListFeature(session).getPermission(new Path(container, "test", EnumSet.of(Path.Type.file)));
        assertTrue(acl.containsKey(new Acl.GroupUser(Acl.GroupUser.EVERYONE)));
        assertTrue(acl.get(new Acl.GroupUser(Acl.GroupUser.EVERYONE)).contains(new Acl.Role(Acl.Role.READ)));
        assertTrue(acl.containsKey(new Acl.Owner("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6")));
        assertTrue(acl.get(new Acl.Owner("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6")).contains(new Acl.Role(Acl.Role.FULL)));
    }

    @Test
    public void testWrite() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(new S3WriteFeature(session, new S3AccessControlListFeature(session)), test, new TransferStatus());
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        final Acl acl = new Acl();
        acl.addAll(new Acl.GroupUser(Acl.GroupUser.EVERYONE), new Acl.Role(Acl.Role.READ));
        acl.addAll(new Acl.GroupUser(Acl.GroupUser.AUTHENTICATED), new Acl.Role(Acl.Role.READ));
        // Check for owner added with full control
        acl.addAll(new Acl.Owner("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6"), new Acl.Role(Acl.Role.FULL));
        f.setPermission(test, acl);
        assertEquals(acl, f.getPermission(test));
        new S3DefaultDeleteFeature(session, new S3AccessControlListFeature(session)).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testWriteVirtualHostBucket() throws Exception {
        final Path test = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3AccessControlListFeature f = new S3AccessControlListFeature(virtualhost);
        new S3TouchFeature(virtualhost).touch(new S3WriteFeature(virtualhost, f), test, new TransferStatus());
        final Acl acl = new Acl();
        acl.addAll(new Acl.Owner("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6"), new Acl.Role(Acl.Role.FULL));
        acl.addAll(new Acl.GroupUser(Acl.GroupUser.EVERYONE), new Acl.Role(Acl.Role.READ));
        acl.addAll(new Acl.GroupUser(Acl.GroupUser.AUTHENTICATED), new Acl.Role(Acl.Role.READ));
        f.setPermission(test, acl);
        assertEquals(acl, f.getPermission(test));
        new S3DefaultDeleteFeature(virtualhost, new S3AccessControlListFeature(virtualhost)).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testReadWithDelimiter() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new S3TouchFeature(session).touch(new S3WriteFeature(session, new S3AccessControlListFeature(session)), new Path(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        assertNotNull(f.getPermission(test));
        new S3DefaultDeleteFeature(session, new S3AccessControlListFeature(session)).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testReadDirectoryPlaceholder() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path placeholder = new S3DirectoryFeature(session).mkdir(new S3WriteFeature(session, new S3AccessControlListFeature(session)), new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertNotNull(acl.getPermission(placeholder));
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(placeholder), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        f.getPermission(test);
    }

    @Test(expected = NotfoundException.class)
    public void testWriteNotFound() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        final Acl acl = new Acl();
        acl.addAll(new Acl.Owner("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6"), new Acl.Role(Acl.Role.FULL));
        acl.addAll(new Acl.GroupUser(Acl.GroupUser.EVERYONE), new Acl.Role(Acl.Role.READ));
        f.setPermission(test, acl);
    }

    @Test
    public void testReadVersioned() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new S3TouchFeature(session).touch(new S3WriteFeature(session, new S3AccessControlListFeature(session)), new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(test));
        try {
            new S3AccessControlListFeature(session).getPermission(test);
        }
        catch(NotfoundException e) {
            fail();
        }
        new S3DefaultDeleteFeature(session, new S3AccessControlListFeature(session)).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testRoles() {
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        assertTrue(f.getAvailableAclUsers(Collections.emptyList()).stream().anyMatch(user -> user instanceof Acl.CanonicalUser));
        assertTrue(f.getAvailableAclUsers(Collections.emptyList()).stream().anyMatch(user -> user instanceof Acl.EmailUser));
    }

    @Test
    public void testCannedLists() {
        assertSame(Acl.CANNED_PRIVATE, S3AccessControlListFeature.toAcl(AccessControlList.REST_CANNED_PRIVATE));
        assertSame(Acl.CANNED_PUBLIC_READ, S3AccessControlListFeature.toAcl(AccessControlList.REST_CANNED_PUBLIC_READ));
        assertSame(Acl.CANNED_PUBLIC_READ_WRITE, S3AccessControlListFeature.toAcl(AccessControlList.REST_CANNED_PUBLIC_READ_WRITE));
        assertSame(Acl.CANNED_AUTHENTICATED_READ, S3AccessControlListFeature.toAcl(AccessControlList.REST_CANNED_AUTHENTICATED_READ));
        assertSame(Acl.CANNED_BUCKET_OWNER_FULLCONTROL, S3AccessControlListFeature.toAcl(AccessControlList.REST_CANNED_BUCKET_OWNER_FULLCONTROL));
        assertSame(Acl.CANNED_BUCKET_OWNER_READ, S3AccessControlListFeature.toAcl(AccessControlList.REST_CANNED_BUCKET_OWNER_READ));

        assertSame(AccessControlList.REST_CANNED_PRIVATE, S3AccessControlListFeature.toObjectAccessControlList(Acl.CANNED_PRIVATE));
        assertEquals(Acl.CANNED_PRIVATE.getCannedString(), S3AccessControlListFeature.toObjectAccessControlList(Acl.CANNED_PRIVATE).getValueForRESTHeaderACL());

        assertSame(AccessControlList.REST_CANNED_PUBLIC_READ, S3AccessControlListFeature.toObjectAccessControlList(Acl.CANNED_PUBLIC_READ));
        assertEquals(Acl.CANNED_PUBLIC_READ.getCannedString(), S3AccessControlListFeature.toObjectAccessControlList(Acl.CANNED_PUBLIC_READ).getValueForRESTHeaderACL());

        assertSame(AccessControlList.REST_CANNED_PUBLIC_READ_WRITE, S3AccessControlListFeature.toObjectAccessControlList(Acl.CANNED_PUBLIC_READ_WRITE));
        assertEquals(Acl.CANNED_PUBLIC_READ_WRITE.getCannedString(), S3AccessControlListFeature.toObjectAccessControlList(Acl.CANNED_PUBLIC_READ_WRITE).getValueForRESTHeaderACL());

        assertSame(AccessControlList.REST_CANNED_AUTHENTICATED_READ, S3AccessControlListFeature.toObjectAccessControlList(Acl.CANNED_AUTHENTICATED_READ));
        assertEquals(Acl.CANNED_AUTHENTICATED_READ.getCannedString(), S3AccessControlListFeature.toObjectAccessControlList(Acl.CANNED_AUTHENTICATED_READ).getValueForRESTHeaderACL());

        assertSame(AccessControlList.REST_CANNED_BUCKET_OWNER_FULLCONTROL, S3AccessControlListFeature.toObjectAccessControlList(Acl.CANNED_BUCKET_OWNER_FULLCONTROL));
        assertEquals(Acl.CANNED_BUCKET_OWNER_FULLCONTROL.getCannedString(), S3AccessControlListFeature.toObjectAccessControlList(Acl.CANNED_BUCKET_OWNER_FULLCONTROL).getValueForRESTHeaderACL());

        assertSame(AccessControlList.REST_CANNED_BUCKET_OWNER_READ, S3AccessControlListFeature.toObjectAccessControlList(Acl.CANNED_BUCKET_OWNER_READ));
        assertEquals(Acl.CANNED_BUCKET_OWNER_READ.getCannedString(), S3AccessControlListFeature.toObjectAccessControlList(Acl.CANNED_BUCKET_OWNER_READ).getValueForRESTHeaderACL());
    }

    @Test
    public void testInvalidOwnerFromServer() {
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        final AccessControlList list = new AccessControlList();
        list.setOwner(new StorageOwner("", ""));
        assertEquals(Acl.EMPTY, S3AccessControlListFeature.toAcl(list));
    }

    @Test
    public void testInvalidOwner() {
        assertNull(S3AccessControlListFeature.toObjectAccessControlList(Acl.EMPTY));
        assertNull(S3AccessControlListFeature.toObjectAccessControlList(new Acl(new Acl.UserAndRole(new Acl.Owner(""), new Acl.Role(Acl.Role.FULL)))));
    }

    @Test
    public void testWriteBucketCannedPublicRead() throws Exception {
        final Path container = new Path(new AlphanumericRandomStringService().random().toLowerCase(Locale.ROOT), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        new S3DirectoryFeature(session).mkdir(new S3WriteFeature(session, f), container, new TransferStatus()
                .setAcl(Acl.CANNED_PRIVATE)
        );
        // S3 turns on all four Block Public Access settings for every new bucket
        assertThrows(AccessDeniedException.class, () -> f.setPermission(container, new TransferStatus().setAcl(Acl.CANNED_PUBLIC_READ)));
    }
}
