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
import ch.cyberduck.core.exception.InteroperabilityException;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * @version $Id$
 */
public class FTPMlsdListServiceTest extends AbstractTestCase {

    @Test(expected = InteroperabilityException.class)
    public void testListNotSupportedTest() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        new LoginConnectionService(new DisabledLoginController(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, Cache.<Path>empty());
        final ListService list = new FTPMlsdListService(session);
        final Path directory = session.workdir();
        list.list(directory, new DisabledListProgressListener());
        session.close();
    }

    @Test(expected = InteroperabilityException.class)
    public void testListNotSupportedSwitch() throws Exception {
        final Host host = new Host(new FTPProtocol(), "mirror.switch.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final FTPSession session = new FTPSession(host);
        new LoginConnectionService(new DisabledLoginController(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, Cache.<Path>empty());
        final ListService list = new FTPMlsdListService(session);
        final Path directory = session.workdir();
        list.list(directory, new DisabledListProgressListener());
        session.close();
    }

    @Test
    public void testList() throws Exception {
        final Host host = new Host(new FTPProtocol(), "ftp.crushftp.com", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setFTPConnectMode(FTPConnectMode.active);
        final FTPSession session = new FTPSession(host);
        new LoginConnectionService(new DisabledLoginController(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, Cache.<Path>empty());
        final ListService s = new FTPMlsdListService(session);
        final Path directory = session.workdir();
        final AttributedList<Path> list = s.list(directory, new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        session.close();
    }
}
