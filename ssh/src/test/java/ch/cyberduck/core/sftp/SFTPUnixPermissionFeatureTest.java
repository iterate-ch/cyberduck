package ch.cyberduck.core.sftp;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SFTPUnixPermissionFeatureTest {

    @Test
    @Ignore
    public void testSetUnixOwner() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final long modified = System.currentTimeMillis();
        final Path test = new Path(home, "test", EnumSet.of(Path.Type.file));
        new SFTPUnixPermissionFeature(session).setUnixOwner(test, "80");
        assertEquals("80", session.list(home, new DisabledListProgressListener()).get(test).attributes().getOwner());
        session.close();
    }

    @Test
    @Ignore
    public void testSetUnixGroup() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final long modified = System.currentTimeMillis();
        final Path test = new Path(home, "test", EnumSet.of(Path.Type.file));
        new SFTPUnixPermissionFeature(session).setUnixGroup(test, "80");
        assertEquals("80", session.list(home, new DisabledListProgressListener()).get(test).attributes().getGroup());
        session.close();
    }

    @Test
    public void testSetUnixPermission() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new SFTPHomeDirectoryService(session).find();
        {
            final Path file = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
            new SFTPTouchFeature(session).touch(file);
            new SFTPUnixPermissionFeature(session).setUnixPermission(file, new Permission(666));
            assertEquals("666", session.list(home, new DisabledListProgressListener()).get(file).attributes().getPermission().getMode());
            new SFTPDeleteFeature(session).delete(Collections.<Path>singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
        {
            final Path directory = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
            new SFTPDirectoryFeature(session).mkdir(directory);
            new SFTPUnixPermissionFeature(session).setUnixPermission(directory, new Permission(666));
            assertEquals("666", session.list(home, new DisabledListProgressListener()).get(directory).attributes().getPermission().getMode());
            new SFTPDeleteFeature(session).delete(Collections.<Path>singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
        session.close();
    }

    @Test
    public void testRetainStickyBits() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path test = new Path(new SFTPHomeDirectoryService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new SFTPTouchFeature(session).touch(test);
        final SFTPUnixPermissionFeature feature = new SFTPUnixPermissionFeature(session);
        feature.setUnixPermission(test,
                new Permission(Permission.Action.all, Permission.Action.read, Permission.Action.read,
                        true, false, false));
        assertEquals(new Permission(Permission.Action.all, Permission.Action.read, Permission.Action.read,
                true, false, false), new SFTPListService(session).list(test.getParent(), new DisabledListProgressListener()).get(
                test).attributes().getPermission());
        feature.setUnixPermission(test,
                new Permission(Permission.Action.all, Permission.Action.read, Permission.Action.read,
                        false, true, false));
        assertEquals(new Permission(Permission.Action.all, Permission.Action.read, Permission.Action.read,
                false, true, false), new SFTPListService(session).list(test.getParent(), new DisabledListProgressListener()).get(
                test).attributes().getPermission());
        feature.setUnixPermission(test,
                new Permission(Permission.Action.all, Permission.Action.read, Permission.Action.read,
                        false, false, true));
        assertEquals(new Permission(Permission.Action.all, Permission.Action.read, Permission.Action.read,
                false, false, true), new SFTPListService(session).list(test.getParent(), new DisabledListProgressListener()).get(
                test).attributes().getPermission());
        new SFTPDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
