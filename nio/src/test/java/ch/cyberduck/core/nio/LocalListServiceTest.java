package ch.cyberduck.core.nio;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.TemporarySupportDirectoryFinder;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class LocalListServiceTest {

    @Test
    public void testList() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final String absolute = new TemporarySupportDirectoryFinder().find().getAbsolute();
        final Path home = new Path(new Path(session.getClient().getPath(absolute).toRealPath().toString(),
                EnumSet.of(Path.Type.directory)), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path file = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path symlinkRelative = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink));
        final Path symlinkAbsolute = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink));
        final Path directory = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new LocalDirectoryFeature(session).mkdir(home, null, new TransferStatus());
        new LocalTouchFeature(session).touch(file, new TransferStatus());
        new LocalSymlinkFeature(session).symlink(symlinkRelative, file.getName());
        new LocalSymlinkFeature(session).symlink(symlinkAbsolute, file.getAbsolute());
        new LocalDirectoryFeature(session).mkdir(directory, null, new TransferStatus());
        final Permission permission = new Permission(Permission.Action.read_write, Permission.Action.read_write, Permission.Action.read_write);
        new LocalUnixPermissionFeature(session).setUnixPermission(file, permission);

        final AttributedList<Path> list = new LocalListService(session).list(home, new DisabledListProgressListener());
        assertTrue(list.contains(file));
        assertEquals(permission, list.get(file).attributes().getPermission());
        assertTrue(list.contains(directory));
        assertTrue(list.contains(symlinkRelative));
        assertEquals(file, list.get(symlinkRelative).getSymlinkTarget());
        assertTrue(list.contains(symlinkAbsolute));
        assertEquals(file, list.get(symlinkAbsolute).getSymlinkTarget());

        new LocalDeleteFeature(session).delete(Arrays.asList(file, symlinkAbsolute, symlinkRelative, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testListNotfound() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path f = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final LocalListService service = new LocalListService(session);
        service.list(f, new DisabledListProgressListener());
    }

    @Test(expected = NotfoundException.class)
    public void testListFile() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final String absolute = new TemporarySupportDirectoryFinder().find().getAbsolute();
        final Path home = new Path(new Path(session.getClient().getPath(absolute).toRealPath().toString(),
                EnumSet.of(Path.Type.directory)), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new LocalDirectoryFeature(session).mkdir(home, null, new TransferStatus());
        final Path f = new Path(home, "test", EnumSet.of(Path.Type.directory));
        final LocalListService service = new LocalListService(session);
        service.list(f, new DisabledListProgressListener());
    }

}