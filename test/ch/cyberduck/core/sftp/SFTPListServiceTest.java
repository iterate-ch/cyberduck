package ch.cyberduck.core.sftp;

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
import ch.cyberduck.core.shared.DefaultHomeFinderService;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SFTPListServiceTest extends AbstractTestCase {

    @Test
    public void testList() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final Path home = new DefaultHomeFinderService(session).find();
        final AttributedList<Path> list = new SFTPListService(session).list(home, new DisabledListProgressListener());
        assertTrue(list.contains(new Path(home, "test", EnumSet.of(Path.Type.file)).getReference()));
        assertEquals(new Permission(Permission.Action.read_write, Permission.Action.read_write, Permission.Action.read_write),
                list.get(new Path(home, "test", EnumSet.of(Path.Type.file)).getReference()).attributes().getPermission());
        assertTrue(list.contains(new Path(home, "test.directory", EnumSet.of(Path.Type.file)).getReference()));
        assertTrue(list.contains(new Path(home, "test.symlink", EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink)).getReference()));
        assertEquals(new Path(home, "test", EnumSet.of(Path.Type.file)), list.get(new Path(home, "test.symlink", EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink)).getReference()).getSymlinkTarget());
        assertTrue(list.contains(new Path(home, "test.symlink-absolute", EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink)).getReference()));
        assertEquals(new Path(home, "test", EnumSet.of(Path.Type.file)), list.get(new Path(home, "test.symlink-absolute", EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink)).getReference()).getSymlinkTarget());
        session.close();
    }

    @Test
    public void testInvalidSymlinkTarget() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        new LoginConnectionService(new DisabledLoginController(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, Cache.<Path>empty());
        final Path home = new DefaultHomeFinderService(session).find();
        final AttributedList<Path> list = new SFTPListService(session).list(home, new DisabledListProgressListener());
        assertTrue(list.contains(new Path(home, "notfound", EnumSet.of(Path.Type.file, Path.Type.symboliclink)).getReference()));
        assertEquals(new Path(home, "test.symlink-invalid", EnumSet.of(Path.Type.file)),
                list.get(new Path(home, "notfound", EnumSet.of(Path.Type.file, Path.Type.symboliclink)).getReference()).getSymlinkTarget());
    }
}
