package ch.cyberduck.core.ftp.list;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.ftp.AbstractFTPTest;
import ch.cyberduck.core.ftp.FTPDeleteFeature;
import ch.cyberduck.core.ftp.FTPDirectoryFeature;
import ch.cyberduck.core.ftp.FTPTouchFeature;
import ch.cyberduck.core.ftp.FTPWorkdirService;
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
public class FTPListServiceTest extends AbstractFTPTest {

    @Test
    public void testList() throws Exception {
        final ListService service = new FTPListService(session, null, TimeZone.getDefault());
        final Path directory = new FTPWorkdirService(session).find();
        final Path file = new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new FTPTouchFeature(session).touch(file, new TransferStatus());
        final AttributedList<Path> list = service.list(directory, new DisabledListProgressListener() {
            @Override
            public void chunk(final Path parent, AttributedList<Path> list) {
                assertFalse(list.isEmpty());
            }
        });
        assertTrue(list.contains(file));
        new FTPDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListExtended() throws Exception {
        final FTPListService service = new FTPListService(session, null, TimeZone.getDefault());
        service.remove(FTPListService.Command.list);
        service.remove(FTPListService.Command.stat);
        service.remove(FTPListService.Command.mlsd);
        final Path directory = new FTPWorkdirService(session).find();
        final Path file = new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new FTPTouchFeature(session).touch(file, new TransferStatus());
        final AttributedList<Path> list = service.list(directory, new DisabledListProgressListener() {
            @Override
            public void chunk(final Path parent, AttributedList<Path> list) {
                assertFalse(list.isEmpty());
            }
        });
        assertTrue(list.contains(file));
        new FTPDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListEmptyDirectoryList() throws Exception {
        final FTPListService list = new FTPListService(session, null, TimeZone.getDefault());
        list.remove(FTPListService.Command.stat);
        list.remove(FTPListService.Command.lista);
        list.remove(FTPListService.Command.mlsd);
        final Path home = new FTPWorkdirService(session).find();
        final Path directory = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new FTPDirectoryFeature(session).mkdir(directory, new TransferStatus());
        final AtomicBoolean callback = new AtomicBoolean();
        assertTrue(list.list(directory, new DisabledListProgressListener() {
            @Override
            public void chunk(final Path parent, final AttributedList<Path> list) {
                assertNotSame(AttributedList.EMPTY, list);
                callback.set(true);
            }
        }).isEmpty());
        assertTrue(callback.get());
        new FTPDeleteFeature(session).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = ConnectionTimeoutException.class)
    public void testListIOFailureStat() throws Exception {
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
                throw new ConnectionTimeoutException("t", new SocketTimeoutException());
            }
        });
        final Path directory = new FTPWorkdirService(session).find();
        final AttributedList<Path> list = service.list(directory, new DisabledListProgressListener());
    }

    @Test(expected = NotfoundException.class)
    public void testListNotfound() throws Exception {
        final Path f = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final FTPListService service = new FTPListService(session, null, TimeZone.getDefault());
        service.list(f, new DisabledListProgressListener());
    }
}
