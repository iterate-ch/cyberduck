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
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

public class LocalListServiceTest {

    @Test
    public void testList() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(Proxy.DIRECT, new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new LocalHomeFinderFeature(session).find();
        final Path file = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path directory = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new LocalDirectoryFeature(session).mkdir(directory, null, new TransferStatus());
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
    public void testListSymlink() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        if(session.isPosixFilesystem()) {
            assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback()));
            assertTrue(session.isConnected());
            assertNotNull(session.getClient());
            session.login(Proxy.DIRECT, new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
            final Path home = new LocalHomeFinderFeature(session).find();
            final Path file = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
            final Path symlinkRelative = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink));
            final Path symlinkAbsolute = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink));
            new LocalTouchFeature(session).touch(file, new TransferStatus());
            new LocalSymlinkFeature(session).symlink(symlinkRelative, file.getName());
            new LocalSymlinkFeature(session).symlink(symlinkAbsolute, file.getAbsolute());
            final AttributedList<Path> list = new LocalListService(session).list(home, new DisabledListProgressListener());
            assertTrue(list.contains(file));
            assertTrue(list.contains(symlinkRelative));
            assertTrue(list.get(symlinkRelative).getSymlinkTarget().getAbsolute().endsWith(file.getAbsolute()));
            assertTrue(list.contains(symlinkAbsolute));
            assertTrue(list.get(symlinkAbsolute).getSymlinkTarget().getAbsolute().endsWith(file.getAbsolute()));
            new LocalDeleteFeature(session).delete(Arrays.asList(file, symlinkAbsolute, symlinkRelative), new DisabledLoginCallback(), new Delete.DisabledCallback());
            session.close();
        }
        else {
            assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback()));
            assertTrue(session.isConnected());
            assertNotNull(session.getClient());
            session.login(Proxy.DIRECT, new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
            final Path home = new LocalHomeFinderFeature(session).find();
            final AttributedList<Path> list = new LocalListService(session).list(home, new DisabledListProgressListener());
            assertTrue(list.contains(new Path(home, "Recent", EnumSet.of(Path.Type.directory))));
            final Path recent = list.get(new Path(home, "Recent", EnumSet.of(Path.Type.directory)));
            assertFalse(recent.attributes().getPermission().isReadable());
            assertTrue(recent.attributes().getPermission().isExecutable());
            try {
                new LocalListService(session).list(recent, new DisabledListProgressListener());
                fail();
            }
            catch(LocalAccessDeniedException e) {
                //
            }
            session.close();
        }
    }

    @Test(expected = NotfoundException.class)
    public void testListNotfound() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path f = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final LocalListService service = new LocalListService(session);
        service.list(f, new DisabledListProgressListener());
    }

    @Test(expected = NotfoundException.class)
    public void testListFile() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new LocalHomeFinderFeature(session).find();
        final LocalListService service = new LocalListService(session);
        service.list(new Path(home, "test", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
    }
}
