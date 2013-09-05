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

import org.junit.Test;

import java.util.TimeZone;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        final ListService list = new FTPListService(session, null, TimeZone.getDefault());
        final Path directory = session.workdir();
        assertTrue(list.list(directory, new DisabledListProgressListener()).contains(
                new Path(directory, "test", Path.FILE_TYPE).getReference()));
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
}
