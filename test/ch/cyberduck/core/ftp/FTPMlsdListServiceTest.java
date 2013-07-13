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

import static org.junit.Assert.assertFalse;

/**
 * @version $Id$
 */
public class FTPMlsdListServiceTest extends AbstractTestCase {

    @Test(expected = BackgroundException.class)
    public void testListNotSupported() throws Exception {
        final Host host = new Host(Protocol.FTP_TLS, "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final ListService list = new FTPMlsdListService(session);
        final Path directory = session.workdir();
        list.list(directory);
    }

    @Test
    public void testList() throws Exception {
        final Host host = new Host(Protocol.FTP, "ftp.crushftp.com", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final ListService s = new FTPMlsdListService(session);
        final Path directory = session.workdir();
        final AttributedList<Path> list = s.list(directory);
        assertFalse(list.isEmpty());
    }
}
