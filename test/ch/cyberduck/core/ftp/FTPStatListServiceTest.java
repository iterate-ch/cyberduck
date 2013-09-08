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
import ch.cyberduck.core.ftp.parser.CompositeFileEntryParser;

import org.apache.commons.net.ftp.parser.UnixFTPEntryParser;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class FTPStatListServiceTest extends AbstractTestCase {

    @Test
    public void testList() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final ListService service = new FTPStatListService(session,
                new CompositeFileEntryParser(Arrays.asList(new UnixFTPEntryParser())));
        final Path directory = session.workdir();
        final AttributedList<Path> list = service.list(directory, new DisabledListProgressListener());
        assertTrue(list.contains(new Path(directory, "test", Path.FILE_TYPE).getReference()));
        assertEquals(new Permission(Permission.Action.read_write, Permission.Action.read_write, Permission.Action.read_write),
                list.get(new Path(directory, "test", Path.FILE_TYPE).getReference()).attributes().getPermission());
        session.close();
    }
}
