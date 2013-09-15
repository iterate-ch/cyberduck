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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;

import org.junit.Test;

import java.net.SocketTimeoutException;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class FTPListServiceTest extends AbstractTestCase {

    @Test
    public void testList() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final ListService service = new FTPListService(session, null, TimeZone.getDefault());
        final Path directory = session.workdir();
        final AttributedList<Path> list = service.list(directory, new DisabledListProgressListener());
        assertTrue(list.contains(
                new Path(directory, "test", Path.FILE_TYPE).getReference()));
        assertEquals(new Permission(Permission.Action.read_write, Permission.Action.read_write, Permission.Action.read_write),
                list.get(new Path(directory, "test", Path.FILE_TYPE).getReference()).attributes().getPermission());
        session.close();
    }

    @Test
    public void testListExtended() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final FTPListService service = new FTPListService(session, null, TimeZone.getDefault());
        service.remove(FTPListService.Command.list);
        service.remove(FTPListService.Command.stat);
        service.remove(FTPListService.Command.mlsd);
        final Path directory = session.workdir();
        final AttributedList<Path> list = service.list(directory, new DisabledListProgressListener());
        assertTrue(list.contains(
                new Path(directory, "test", Path.FILE_TYPE).getReference()));
        assertEquals(new Permission(Permission.Action.read_write, Permission.Action.read_write, Permission.Action.read_write),
                list.get(new Path(directory, "test", Path.FILE_TYPE).getReference()).attributes().getPermission());
        session.close();
    }

    @Test
    public void testListEmptyDirectoryStat() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final FTPListService list = new FTPListService(session, null, TimeZone.getDefault());
        list.remove(FTPListService.Command.list);
        list.remove(FTPListService.Command.lista);
        list.remove(FTPListService.Command.mlsd);
        assertTrue(list.list(new Path(session.workdir(), "test.d", Path.DIRECTORY_TYPE), new DisabledListProgressListener()).isEmpty());
        session.close();
    }

    @Test
    public void testListEmptyDirectoryList() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final FTPListService list = new FTPListService(session, null, TimeZone.getDefault());
        list.remove(FTPListService.Command.stat);
        list.remove(FTPListService.Command.lista);
        list.remove(FTPListService.Command.mlsd);
        assertTrue(list.list(new Path(session.workdir(), "test.d", Path.DIRECTORY_TYPE), new DisabledListProgressListener()).isEmpty());
        session.close();
    }

    @Test
    public void testPostProcessing() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final FTPListService service = new FTPListService(session, null, TimeZone.getDefault());
        final AttributedList<Path> list = new AttributedList<Path>();
        list.add(new Path("/test.d", Path.SYMBOLIC_LINK_TYPE | Path.FILE_TYPE));
        assertTrue(list.contains(new Path("/test.d", Path.SYMBOLIC_LINK_TYPE | Path.FILE_TYPE).getReference()));
        service.post(new Path("/", Path.DIRECTORY_TYPE), list);
        assertFalse(list.contains(new Path("/test.d", Path.SYMBOLIC_LINK_TYPE | Path.FILE_TYPE).getReference()));
        assertTrue(list.contains(new Path("/test.d", Path.SYMBOLIC_LINK_TYPE | Path.DIRECTORY_TYPE).getReference()));
        session.close();
    }

    @Test
    public void testListIOFailureStat() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
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
        final Path directory = session.workdir();
        final AttributedList<Path> list = service.list(directory, new DisabledListProgressListener());
        assertTrue(set.get());
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        assertTrue(list.contains(
                new Path(directory, "test", Path.FILE_TYPE).getReference()));
        service.list(directory, new DisabledListProgressListener());
    }
}
