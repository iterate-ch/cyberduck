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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ftp.parser.CompositeFileEntryParser;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.net.ftp.parser.UnixFTPEntryParser;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class FTPDefaultListServiceTest {

    @Test
    public void testListDefault() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final ListService list = new FTPDefaultListService(session, new DisabledPasswordStore(), new DisabledLoginCallback(),
                new CompositeFileEntryParser(Arrays.asList(new UnixFTPEntryParser())),
                FTPListService.Command.list);
        final Path directory = new FTPWorkdirService(session).find();
        assertTrue(list.list(directory, new DisabledListProgressListener()).contains(new Path(directory, "test", EnumSet.of(Path.Type.file))));
        session.close();
    }

    @Test
    public void testListDefaultFlag() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final ListService list = new FTPDefaultListService(session, new DisabledPasswordStore(), new DisabledLoginCallback(),
                new CompositeFileEntryParser(Arrays.asList(new UnixFTPEntryParser())),
                FTPListService.Command.lista);
        final Path directory = new FTPWorkdirService(session).find();
        assertTrue(list.list(directory, new DisabledListProgressListener()).contains(new Path(directory, "test", EnumSet.of(Path.Type.file))));
        session.close();
    }
}
