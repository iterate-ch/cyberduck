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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

import org.junit.Test;

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
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path home = new DefaultHomeFinderService(session).find();
        final AttributedList<Path> list = new SFTPListService(session).list(home, new DisabledListProgressListener());
        assertTrue(list.contains(new Path(home, "test", Path.FILE_TYPE).getReference()));
        assertTrue(list.contains(new Path(home, "test.directory", Path.FILE_TYPE).getReference()));
        assertTrue(list.contains(new Path(home, "test.symlink", Path.FILE_TYPE | Path.SYMBOLIC_LINK_TYPE).getReference()));
        assertEquals(new Path(home, "test", Path.FILE_TYPE), list.get(new Path(home, "test.symlink", Path.FILE_TYPE | Path.SYMBOLIC_LINK_TYPE).getReference()).getSymlinkTarget());
        assertTrue(list.contains(new Path(home, "test.symlink-absolute", Path.FILE_TYPE | Path.SYMBOLIC_LINK_TYPE).getReference()));
        assertEquals(new Path(home, "test", Path.FILE_TYPE), list.get(new Path(home, "test.symlink-absolute", Path.FILE_TYPE | Path.SYMBOLIC_LINK_TYPE).getReference()).getSymlinkTarget());
        session.close();
    }
}
