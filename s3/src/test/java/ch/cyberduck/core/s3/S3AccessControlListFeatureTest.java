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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3AccessControlListFeatureTest {

    @Test
    public void testReadContainer() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Acl acl = new S3AccessControlListFeature(session).getPermission(container);
        assertTrue(acl.containsKey(new Acl.GroupUser("http://acs.amazonaws.com/groups/s3/LogDelivery")));
        assertTrue(acl.get(new Acl.GroupUser("http://acs.amazonaws.com/groups/s3/LogDelivery")).contains(
                new Acl.Role(Acl.Role.WRITE)
        ));
        assertTrue(acl.get(new Acl.GroupUser("http://acs.amazonaws.com/groups/s3/LogDelivery")).contains(
                new Acl.Role("READ_ACP")
        ));
        assertTrue(acl.containsKey(new Acl.CanonicalUser("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6")));
        assertTrue(acl.get(new Acl.CanonicalUser("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6")).contains(
                new Acl.Role(Acl.Role.FULL)
        ));
        session.close();
    }

    @Test
    public void testReadKey() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Acl acl = new S3AccessControlListFeature(session).getPermission(new Path(container, "test.txt", EnumSet.of(Path.Type.file)));
        assertTrue(acl.containsKey(new Acl.GroupUser("http://acs.amazonaws.com/groups/global/AllUsers")));
        assertTrue(acl.get(new Acl.GroupUser("http://acs.amazonaws.com/groups/global/AllUsers")).contains(
                new Acl.Role(Acl.Role.READ)
        ));
        assertTrue(acl.containsKey(new Acl.CanonicalUser("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6")));
        assertTrue(acl.get(new Acl.CanonicalUser("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6")).contains(
                new Acl.Role(Acl.Role.FULL)
        ));
        session.close();
    }

    @Test
    public void testWrite() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(test);
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        {
            final Acl acl = new Acl();
            acl.addAll(new Acl.GroupUser(Acl.GroupUser.EVERYONE), new Acl.Role(Acl.Role.READ));
            acl.addAll(new Acl.GroupUser(Acl.GroupUser.AUTHENTICATED), new Acl.Role(Acl.Role.READ));
            f.setPermission(test, acl);
        }
        {
            final Acl acl = new Acl();
            acl.addAll(new Acl.GroupUser("http://acs.amazonaws.com/groups/global/AllUsers"), new Acl.Role(Acl.Role.READ));
            acl.addAll(new Acl.GroupUser("http://acs.amazonaws.com/groups/global/AuthenticatedUsers"), new Acl.Role(Acl.Role.READ));
            // Check for owner added with full control
            acl.addAll(new Acl.CanonicalUser("80b9982b7b08045ee86680cc47f43c84bf439494a89ece22b5330f8a49477cf6"), new Acl.Role(Acl.Role.FULL));
            assertEquals(acl, f.getPermission(test));
        }
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        session.close();
    }

    @Test
    public void testReadWithDelimiter() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path placeholder = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        final Path test = new Path(placeholder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(test);
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        assertNotNull(f.getPermission(test));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        session.close();
    }

    @Test
    public void testReadDirectoryPlaceholder() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path placeholder = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        new S3DirectoryFeature(session).mkdir(placeholder);
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        assertNotNull(f.getPermission(placeholder));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(placeholder), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        f.getPermission(test);
    }

    @Test(expected = NotfoundException.class)
    public void testWriteNotFound() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        f.setPermission(test, Acl.EMPTY);
    }

    @Test
    public void testRoles() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        final S3AccessControlListFeature f = new S3AccessControlListFeature(session);
        assertTrue(f.getAvailableAclUsers().contains(new Acl.CanonicalUser()));
        assertTrue(f.getAvailableAclUsers().contains(new Acl.EmailUser()));
    }
}
