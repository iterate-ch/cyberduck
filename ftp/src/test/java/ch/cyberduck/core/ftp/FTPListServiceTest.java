package ch.cyberduck.core.ftp;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class FTPListServiceTest {

    @Test
    public void testList() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final ListService service = new FTPListService(session, null, TimeZone.getDefault());
        final Path directory = new FTPWorkdirService(session).find();
        final AttributedList<Path> list = service.list(directory, new DisabledListProgressListener() {
            @Override
            public void chunk(final Path parent, AttributedList<Path> list) throws ListCanceledException {
                assertFalse(list.isEmpty());
            }
        });
        assertTrue(list.contains(
                new Path(directory, "test", EnumSet.of(Path.Type.file))));
        assertEquals(new Permission(Permission.Action.read_write, Permission.Action.read_write, Permission.Action.read_write),
                list.get(new Path(directory, "test", EnumSet.of(Path.Type.file))).attributes().getPermission());
        session.close();
    }

    @Test
    public void testListExtended() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final FTPListService service = new FTPListService(session, null, TimeZone.getDefault());
        service.remove(FTPListService.Command.list);
        service.remove(FTPListService.Command.stat);
        service.remove(FTPListService.Command.mlsd);
        final Path directory = new FTPWorkdirService(session).find();
        final AttributedList<Path> list = service.list(directory, new DisabledListProgressListener() {
            @Override
            public void chunk(final Path parent, AttributedList<Path> list) throws ListCanceledException {
                assertFalse(list.isEmpty());
            }
        });
        assertTrue(list.contains(
                new Path(directory, "test", EnumSet.of(Path.Type.file))));
        assertEquals(new Permission(Permission.Action.read_write, Permission.Action.read_write, Permission.Action.read_write),
                list.get(new Path(directory, "test", EnumSet.of(Path.Type.file))).attributes().getPermission());
        session.close();
    }

    @Test
    public void testListEmptyDirectoryStat() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final FTPListService list = new FTPListService(session, null, TimeZone.getDefault());
        list.remove(FTPListService.Command.list);
        list.remove(FTPListService.Command.lista);
        list.remove(FTPListService.Command.mlsd);
        final Path home = new FTPWorkdirService(session).find();
        final Path directory = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new FTPDirectoryFeature(session).mkdir(directory);
        assertTrue(list.list(directory, new DisabledListProgressListener()).isEmpty());
        new FTPDeleteFeature(session).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testListEmptyDirectoryList() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final FTPListService list = new FTPListService(session, null, TimeZone.getDefault());
        list.remove(FTPListService.Command.stat);
        list.remove(FTPListService.Command.lista);
        list.remove(FTPListService.Command.mlsd);
        final Path home = new FTPWorkdirService(session).find();
        final Path directory = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new FTPDirectoryFeature(session).mkdir(directory);
        assertTrue(list.list(directory, new DisabledListProgressListener()).isEmpty());
        new FTPDeleteFeature(session).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testListIOFailureStat() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final FTPListService service = new FTPListService(session, null, TimeZone.getDefault());
        service.remove(FTPListService.Command.lista);
        service.remove(FTPListService.Command.mlsd);
        final AtomicBoolean set = new AtomicBoolean();
        service.implementations.put(FTPListService.Command.stat, new ListService() {
            @Override
            public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
                if(set.get()) {
                    fail();
                }
                set.set(true);
                throw new BackgroundException("t", new SocketTimeoutException());
            }
        });
        final Path directory = new FTPWorkdirService(session).find();
        final Path file = new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature<Integer>(new DefaultUploadFeature<Integer>(new FTPWriteFeature(session))).touch(file, new TransferStatus());
        final AttributedList<Path> list = service.list(directory, new DisabledListProgressListener());
        assertTrue(set.get());
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        assertTrue(list.contains(file));
        service.list(directory, new DisabledListProgressListener());
        new FTPDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testListNotfound() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path f = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final FTPListService service = new FTPListService(session, null, TimeZone.getDefault());
        service.list(f, new DisabledListProgressListener());
    }
}
