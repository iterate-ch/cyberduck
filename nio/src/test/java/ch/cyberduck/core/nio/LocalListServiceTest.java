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
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class LocalListServiceTest {

    @Test
    public void testList() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        assertNotNull(session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new LocalHomeFinderFeature().find();
        final Path file = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path directory = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new LocalDirectoryFeature(session).mkdir(directory, new TransferStatus());
        new LocalTouchFeature(session).touch(file, new TransferStatus());
        final AttributedList<Path> list = new LocalListService(session).list(home, new DisabledListProgressListener());
        assertTrue(list.contains(file));
        assertTrue(list.contains(directory));
        new LocalDeleteFeature(session).delete(Arrays.asList(file, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new LocalListService(session).list(home, new DisabledListProgressListener()).contains(file));
        assertFalse(new LocalListService(session).list(home, new DisabledListProgressListener()).contains(directory));
        session.close();
    }

    @Test
    public void testDifferentDrive() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        if(!session.isPosixFilesystem()) {
            assertNotNull(session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback()));
            assertTrue(session.isConnected());
            assertNotNull(session.getClient());
            session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
            final Path test = new Path("/D:/", EnumSet.of(Path.Type.directory, Path.Type.volume));
            final AttributedList<Path> list = new LocalListService(session).list(test, new DisabledListProgressListener());
            assertNotSame(AttributedList.emptyList(), list);
        }
        session.close();
    }

    @Test
    public void testListSymlink() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        assumeTrue(session.isPosixFilesystem());
        assertNotNull(session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new LocalHomeFinderFeature().find();
        final Path file = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path symlinkRelative = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink));
        final Path symlinkAbsolute = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink));
        new LocalTouchFeature(session).touch(file, new TransferStatus());
        new LocalSymlinkFeature(session).symlink(symlinkRelative, file.getName());
        new LocalSymlinkFeature(session).symlink(symlinkAbsolute, file.getAbsolute());
        final AttributedList<Path> list = new LocalListService(session).list(home, new DisabledListProgressListener());
        assertTrue(list.contains(file));
        assertTrue(list.contains(symlinkRelative));
        assertFalse(list.get(symlinkRelative).getSymlinkTarget().getAbsolute().endsWith(file.getAbsolute()));
        assertTrue(list.get(symlinkRelative).getSymlinkTarget().getAbsolute().endsWith(file.getName()));
        assertTrue(list.contains(symlinkAbsolute));
        assertTrue(list.get(symlinkAbsolute).getSymlinkTarget().getAbsolute().endsWith(file.getAbsolute()));
        new LocalDeleteFeature(session).delete(Arrays.asList(file, symlinkAbsolute, symlinkRelative), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testListJunction() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        assumeTrue(Factory.Platform.getDefault().equals(Factory.Platform.Name.windows));
        assertNotNull(session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new LocalHomeFinderFeature().find();
        final AttributedList<Path> list = new LocalListService(session).list(home, new DisabledListProgressListener());
        assertTrue(list.contains(new Path(home, "Recent", EnumSet.of(Path.Type.directory))));
        final Path recent = list.get(new Path(home, "Recent", EnumSet.of(Path.Type.directory)));
        assertFalse(recent.attributes().getPermission().isReadable());
        assertTrue(recent.attributes().getPermission().isExecutable());
        try {
            new LocalListService(session).list(recent, new DisabledListProgressListener());
            fail();
        }
        catch(AccessDeniedException | NotfoundException e) {
            //
        }
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testListNotfound() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path f = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final LocalListService service = new LocalListService(session);
        service.list(f, new DisabledListProgressListener());
    }

    @Test(expected = NotfoundException.class)
    public void testListFile() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new LocalHomeFinderFeature().find();
        final LocalListService service = new LocalListService(session);
        service.list(new Path(home, "test", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
    }
}
