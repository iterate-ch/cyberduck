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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.proxy.Proxy;
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
    public void testReadBucketDisabledAcl() throws Exception {
        final Path container = new Path("test-eu-central-1-acl-disabled", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        assertNotEquals(Acl.EMPTY, f.getPermission(container));
        assertEquals(Acl.EMPTY, f.getDefault(container, null));
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
        new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(test, new TransferStatus());
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        final Acl acl = new Acl();
        acl.addAll(new Acl.GroupUser(Acl.GroupUser.EVERYONE), new Acl.Role(Acl.Role.READ));
        acl.addAll(new Acl.GroupUser(Acl.GroupUser.AUTHENTICATED), new Acl.Role(Acl.Role.READ));
        // Check for owner added with full control
        acl.addAll(new Acl.Owner("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6"), new Acl.Role(Acl.Role.FULL));
        f.setPermission(test, acl);
        assertEquals(acl, f.getPermission(test));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteVirtualHostBucket() throws Exception {
        final Path test = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(virtualhost, new S3AccessControlListFeature(session)).touch(test, new TransferStatus());
        final S3AccessControlListFeature f = new S3AccessControlListFeature(virtualhost);
        final Acl acl = new Acl();
        acl.addAll(new Acl.Owner("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6"), new Acl.Role(Acl.Role.FULL));
        acl.addAll(new Acl.GroupUser(Acl.GroupUser.EVERYONE), new Acl.Role(Acl.Role.READ));
        acl.addAll(new Acl.GroupUser(Acl.GroupUser.AUTHENTICATED), new Acl.Role(Acl.Role.READ));
        f.setPermission(test, acl);
        assertEquals(acl, f.getPermission(test));
        new S3DefaultDeleteFeature(virtualhost).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteMinio() throws Exception {
        final Host host = new Host(new S3Protocol(), "play.min.io", new Credentials(
                "Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG"
        )) {
            @Override
            public String getProperty(final String key) {
                if("s3.bucket.virtualhost.disable".equals(key)) {
                    return String.valueOf(true);
                }
                return super.getProperty(key);
            }
        };
        final S3Session session = new S3Session(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path(String.format("cd-%s", new AlphanumericRandomStringService().random().toLowerCase(Locale.getDefault())), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new S3BucketCreateService(session).create(container, null);
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(test, new TransferStatus());
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        {
            final Acl acl = new Acl();
            acl.addAll(new Acl.GroupUser(Acl.GroupUser.EVERYONE), new Acl.Role(Acl.Role.READ));
            acl.addAll(new Acl.GroupUser(Acl.GroupUser.AUTHENTICATED), new Acl.Role(Acl.Role.READ));
            try {
                f.setPermission(test, acl);
                fail();
            }
            catch(InteroperabilityException e) {
                //
            }
        }
        assertEquals(Acl.EMPTY, f.getPermission(test));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadWithDelimiter() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(new Path(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        assertNotNull(f.getPermission(test));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadDirectoryPlaceholder() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path placeholder = new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertNotNull(acl.getPermission(placeholder));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
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
        final Path test = new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(test));
        try {
            new S3AccessControlListFeature(session).getPermission(test);
        }
        catch(NotfoundException e) {
            fail();
        }
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testRoles() {
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        assertTrue(f.getAvailableAclUsers().stream().filter(user -> user instanceof Acl.CanonicalUser).findAny().isPresent());
        assertTrue(f.getAvailableAclUsers().stream().filter(user -> user instanceof Acl.EmailUser).findAny().isPresent());
    }

    @Test
    public void testCannedLists() {
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        assertSame(Acl.CANNED_PRIVATE, f.toAcl(AccessControlList.REST_CANNED_PRIVATE));
        assertSame(Acl.CANNED_PUBLIC_READ, f.toAcl(AccessControlList.REST_CANNED_PUBLIC_READ));
        assertSame(Acl.CANNED_PUBLIC_READ_WRITE, f.toAcl(AccessControlList.REST_CANNED_PUBLIC_READ_WRITE));
        assertSame(Acl.CANNED_AUTHENTICATED_READ, f.toAcl(AccessControlList.REST_CANNED_AUTHENTICATED_READ));
        assertSame(Acl.CANNED_BUCKET_OWNER_FULLCONTROL, f.toAcl(AccessControlList.REST_CANNED_BUCKET_OWNER_FULLCONTROL));
        assertSame(Acl.CANNED_BUCKET_OWNER_READ, f.toAcl(AccessControlList.REST_CANNED_BUCKET_OWNER_READ));

        assertSame(AccessControlList.REST_CANNED_PRIVATE, f.toAcl(Acl.CANNED_PRIVATE));
        assertEquals(Acl.CANNED_PRIVATE.getCannedString(), f.toAcl(Acl.CANNED_PRIVATE).getValueForRESTHeaderACL());

        assertSame(AccessControlList.REST_CANNED_PUBLIC_READ, f.toAcl(Acl.CANNED_PUBLIC_READ));
        assertEquals(Acl.CANNED_PUBLIC_READ.getCannedString(), f.toAcl(Acl.CANNED_PUBLIC_READ).getValueForRESTHeaderACL());

        assertSame(AccessControlList.REST_CANNED_PUBLIC_READ_WRITE, f.toAcl(Acl.CANNED_PUBLIC_READ_WRITE));
        assertEquals(Acl.CANNED_PUBLIC_READ_WRITE.getCannedString(), f.toAcl(Acl.CANNED_PUBLIC_READ_WRITE).getValueForRESTHeaderACL());

        assertSame(AccessControlList.REST_CANNED_AUTHENTICATED_READ, f.toAcl(Acl.CANNED_AUTHENTICATED_READ));
        assertEquals(Acl.CANNED_AUTHENTICATED_READ.getCannedString(), f.toAcl(Acl.CANNED_AUTHENTICATED_READ).getValueForRESTHeaderACL());

        assertSame(AccessControlList.REST_CANNED_BUCKET_OWNER_FULLCONTROL, f.toAcl(Acl.CANNED_BUCKET_OWNER_FULLCONTROL));
        assertEquals(Acl.CANNED_BUCKET_OWNER_FULLCONTROL.getCannedString(), f.toAcl(Acl.CANNED_BUCKET_OWNER_FULLCONTROL).getValueForRESTHeaderACL());

        assertSame(AccessControlList.REST_CANNED_BUCKET_OWNER_READ, f.toAcl(Acl.CANNED_BUCKET_OWNER_READ));
        assertEquals(Acl.CANNED_BUCKET_OWNER_READ.getCannedString(), f.toAcl(Acl.CANNED_BUCKET_OWNER_READ).getValueForRESTHeaderACL());
    }

    @Test
    public void testInvalidOwnerFromServer() {
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        final AccessControlList list = new AccessControlList();
        list.setOwner(new StorageOwner("", ""));
        assertEquals(Acl.EMPTY, f.toAcl(list));
    }

    @Test
    public void testInvalidOwner() {
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        assertNull(f.toAcl(Acl.EMPTY));
        assertNull(f.toAcl(new Acl(new Acl.UserAndRole(new Acl.Owner(""), new Acl.Role(Acl.Role.FULL)))));
    }
}
